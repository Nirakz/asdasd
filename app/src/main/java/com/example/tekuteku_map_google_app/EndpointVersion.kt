package com.example.tekuteku_map_google_app

import androidx.compose.runtime.Composable
import com.squareup.moshi.JsonClass
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import java.net.URL
import okhttp3.Request
import com.squareup.moshi.Moshi
import okhttp3.RequestBody.Companion.toRequestBody

@JsonClass(generateAdapter = true)
data class MetaDataVersion(
    val masterData: Int,
    val game: Int,
    val geoblocks: Int,
    val maps: Int,
    val resource: Int,
    val notes: Int,
    val lifelog: Int
)

@JsonClass(generateAdapter = true)
data class MetaData(
    val endpointPrefix: String,
    val eApiEndpoint: String,
    val masterDataEndpoint: String,
    val geoblocksEndpoint: String,
    val mapsEndpoint: String,
    val resourceEndpoint: String,
    val notesEndpoint: String,
    val prev: MetaDataVersion,
    val curr: MetaDataVersion,
    val next: MetaDataVersion
)

@JsonClass(generateAdapter = true)
data class Endpoints (
    val eapi: String,
    val geoblocks: String,
    val  maps: String,
)

const val ENDPOINT_VERSION_SUBSTITUTION = "${'$'}version"

enum class HttpStatusCode(val code: Int) {
    OK(200),
    NO_CONTENT(204),
    MULTIPLE_CHOICES(300),
    BAD_REQUEST(400),
    INTERNAL_SERVER_ERROR(500)
}


fun createUrl(endpointPrefix: String, endpoint: String, version: Int): String {
    val updatedEndpoint = endpoint.replace(ENDPOINT_VERSION_SUBSTITUTION, version.toString())
    return URL("$endpointPrefix$updatedEndpoint").toString()
}

fun metaDataToEndpoints(metaData: MetaData): Endpoints {
    val appVersion = defaultConfig.appVersion.toInt()
    val metaDataVersion = when {
        metaData.curr.lifelog == appVersion -> metaData.curr
        metaData.next.lifelog <= appVersion -> metaData.next
        metaData.prev.lifelog <= appVersion -> metaData.prev
        else -> null
    }

    if (metaDataVersion == null) {
        throw DeprecatedAppVersionError("DeprecatedAppVersion", mapOf("appVersion" to appVersion, "metaData" to metaData))
    }

    return Endpoints(
        eapi = defaultConfig.api.eapi.baseUrl
            ?: createUrl(metaData.endpointPrefix, metaData.eApiEndpoint, metaDataVersion.game),
        geoblocks = createUrl(metaData.endpointPrefix, metaData.geoblocksEndpoint, metaDataVersion.geoblocks),
        maps = createUrl(metaData.endpointPrefix, metaData.mapsEndpoint, metaDataVersion.maps)
    )
}

suspend fun handleError(response: okhttp3.Response) {
    if (response.code in HttpStatusCode.OK.code until HttpStatusCode.MULTIPLE_CHOICES.code) {
        return
    }

    val body = try {
        response.body?.string()
    } catch (_: Exception) {
        null
    }

    when {
        response.code >= HttpStatusCode.INTERNAL_SERVER_ERROR.code ->
            throw ApiRequestError("Server Error on ${response.request.url}", response.code, body)
        response.code in HttpStatusCode.BAD_REQUEST.code until HttpStatusCode.INTERNAL_SERVER_ERROR.code ->
            throw ApiRequestError("Client Error on ${response.request.url}", response.code, body)
    }
}

data class CallApiOptions(
    val method: String = "GET",
    val urlParams: Map<String, String> = emptyMap(),
    val query: Map<String, Any?> = emptyMap(),
    val body: Any? = null,
    val headers: Map<String, String> = emptyMap()
)

val moshi: Moshi = Moshi.Builder().build()
suspend inline fun <reified R> callApi(
    baseUrl: String,
    endpoint: String,
    options: CallApiOptions = CallApiOptions()
): R {
    val client = OkHttpClient()

    val urlBuilder = "$baseUrl$endpoint".toHttpUrl().newBuilder()
    options.query.forEach { (key, value) ->
        when (value) {
            is List<*> -> value.forEach { urlBuilder.addQueryParameter(key, it.toString()) }
            else -> urlBuilder.addQueryParameter(key, value.toString())
        }
    }

    val requestBuilder = Request.Builder()
        .url(urlBuilder.build())

    when {
        options.body is FormBody -> requestBuilder.method(options.method, options.body)
        options.body != null -> {
            val jsonBody = moshi.adapter(Any::class.java).toJson(options.body)
            requestBuilder
                .header("Content-Type", "application/json")
                .method(options.method, jsonBody.toRequestBody())
        }
        else -> requestBuilder.method(options.method, null)
    }

    options.headers.forEach { (key, value) -> requestBuilder.header(key, value) }

    val response = client.newCall(requestBuilder.build()).execute()
    handleError(response)

    return when {
        response.code == HttpStatusCode.NO_CONTENT.code -> null as R
        else -> moshi.adapter(R::class.java).fromJson(response.body?.string() ?: "")
            ?: throw IllegalStateException("Failed to parse response")
    }
}

suspend fun endpointVersionProvider(): Endpoints {
    return try {
        val useMock = defaultConfig.api.meta.baseUrl.isEmpty() && defaultConfig.api.meta.mockMetaData != null
        val result = if (useMock) {
            defaultConfig.api.meta.mockMetaData as MetaData
        } else {
            callApi<MetaData>(defaultConfig.api.meta.baseUrl, defaultConfig.api.meta.endpoint)
        }
        metaDataToEndpoints(result)
    } catch (error: Exception) {
        throw error
    }
}
