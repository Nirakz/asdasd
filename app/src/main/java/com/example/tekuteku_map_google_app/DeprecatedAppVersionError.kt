package com.example.tekuteku_map_google_app


open class BaseError(message: String? = null, private var detail: Any? = null) :
    Exception(message) {
}

class ApiRequestError(message: String, val status: Int, detail: Any? = null) : BaseError(message, detail)

class DeprecatedAppVersionError(s: String, mapOf: Map<String, Any>) : BaseError()