package com.example.tekuteku_map_google_app

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val appVersion: String,
    val officialTwitterUrl: String,
    val inquiryUrl: String,
    val firebaseConfig: Map<String, @Contextual Any>,
    val api: Api
)

@Serializable
data class Api(
    val meta: Meta,
    val eapi: Eapi
)

@Serializable
data class Meta(
    val baseUrl: String,
    val endpoint: String,
    val mockMetaData: Map<String, @Contextual Any>?
)

@Serializable
data class Eapi(
    val baseUrl: String?
)

val defaultConfig = Config(
    appVersion = "0",
    officialTwitterUrl = "https://twitter.com/teku_4",
    inquiryUrl = "https://help.tekutekulife.com/hc/ja/requests/new",
    firebaseConfig = emptyMap(),
    api = Api(
        meta = Meta(
            baseUrl = "https://dev.tekutekulife.com/meta",
            // Production
            // baseUrl = "https://prod.tekutekulife.com/meta",
            endpoint = "/data.json",
            mockMetaData = null
        ),
        eapi = Eapi(
            baseUrl = null
        )
    )
)