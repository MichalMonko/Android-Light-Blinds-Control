package com.example.smarthomecontrol.networking


enum class RequestType {
    GET, POST
}

enum class ErrorType {
    CONNECTION_ERROR, OTHER_ERROR
}

interface RequestReceiver {
    fun onSuccess(requestType: RequestType, data: String?)
    fun onError(requestType: RequestType, error: ErrorType)
}