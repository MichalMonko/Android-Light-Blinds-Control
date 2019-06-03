package com.example.smarthomecontrol.networking

import com.example.smarthomecontrol.data_classes.SensorsData


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