package com.example.smarthomecontrol.networking

import android.util.Log
import java.io.IOException
import java.lang.Exception
import java.net.CacheResponse
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "GET_TASK"

class GetTask(val url: URL) {

    fun handleGetResult(response: String, resultCode: RESULT) {
        RequestExecutor.handleGetResult(resultCode, response)
    }
}

class GetTaskRunnable(private val task: GetTask) : Runnable {

    override fun run() {
        var jsonResponse = ""
        var result = RESULT.GET_OK
        try {
            with(task.url.openConnection() as HttpURLConnection)
            {
                requestMethod = "GET"
                jsonResponse = inputStream.bufferedReader().readText()
            }
        } catch (ex: IOException) {
            Log.e(TAG, "IOException during get request")
            jsonResponse = ""
            result = RESULT.GET_CONNECTION_ERROR
        } catch (ex: Exception) {
            Log.e(TAG, "Other error during get request ${ex.printStackTrace()}")
            result = RESULT.GET_OTHER_ERROR
        } finally {
            task.handleGetResult(jsonResponse, result)
        }
    }
}

class GetTaskBuilder {
    private lateinit var url: URL

    fun withURL(url: URL): GetTaskBuilder {
        this.url = url
        return this
    }

    fun getRunnable(): GetTaskRunnable {
        return GetTaskRunnable(GetTask(this.url))
    }
}