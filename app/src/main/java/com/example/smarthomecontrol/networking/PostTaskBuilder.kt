package com.example.smarthomecontrol.networking

import android.util.Log
import com.example.smarthomecontrol.data_classes.PostData
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "POST_TASK"

class PostTask(val url: URL, val data: PostData) {

    fun handlePostResult(result: RESULT): Unit {
        RequestExecutor.handlePostResult(result)
    }
}

class PostTaskRunnable(private val postTask: PostTask) : Runnable {
    override fun run() {
        try {
            with(postTask.url.openConnection() as HttpURLConnection)
            {
                requestMethod = "POST"
                this.setRequestProperty("Content-Type", "application/json")

                val outputWriter = OutputStreamWriter(outputStream)
                outputWriter.write(postTask.data.jsonRepresentation)
                outputWriter.flush()
                outputWriter.close()

                val result =
                    if (this.responseCode == HttpURLConnection.HTTP_OK) RESULT.POST_OK else RESULT.POST_CONNECTION_ERROR
                postTask.handlePostResult(result)
            }
        } catch (ex: IOException) {
            Log.e(TAG, "IOException during post request")
            postTask.handlePostResult(RESULT.POST_CONNECTION_ERROR)
        } catch (ex: Exception) {
            Log.e(TAG, "Other exception during post request: ${ex.printStackTrace()}")
            postTask.handlePostResult(RESULT.POST_OTHER_ERROR)
        }
    }
}

class PostTaskBuilder {
    private lateinit var url: URL
    private lateinit var postData: PostData
    private lateinit var executor: RequestExecutor


    fun withURL(to: URL): PostTaskBuilder {
        url = to
        return this
    }

    fun withData(data: PostData): PostTaskBuilder {
        postData = data
        return this
    }

    fun getRunnable(): PostTaskRunnable {
        return PostTaskRunnable(PostTask(url, postData))
    }

}