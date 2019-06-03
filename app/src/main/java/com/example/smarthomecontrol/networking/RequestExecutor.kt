package com.example.smarthomecontrol.networking

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.example.smarthomecontrol.data_classes.PostData
import java.net.URL
import java.util.concurrent.*

private const val TAG = "NetworkingExecutor"

enum class RESULT(val value: Int) {
    POST_OK(1),
    POST_OTHER_ERROR(2),
    POST_CONNECTION_ERROR(3),
    GET_OK(5),
    GET_OTHER_ERROR(6),
    GET_CONNECTION_ERROR(7)
}

object RequestExecutor {
    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            Log.d(TAG, "handleMessage started")
            when (msg?.what) {
                RESULT.POST_OK.value -> listener?.onSuccess(RequestType.POST, null)
                RESULT.GET_OK.value -> listener?.onSuccess(RequestType.GET, msg.obj as String?)
                RESULT.GET_CONNECTION_ERROR.value -> listener?.onError(RequestType.GET, ErrorType.CONNECTION_ERROR)
                RESULT.GET_OTHER_ERROR.value -> listener?.onError(RequestType.GET, ErrorType.OTHER_ERROR)
                RESULT.POST_CONNECTION_ERROR.value -> listener?.onError(RequestType.POST, ErrorType.CONNECTION_ERROR)
                RESULT.POST_OTHER_ERROR.value -> listener?.onError(RequestType.POST, ErrorType.OTHER_ERROR)
            }
            Log.d(TAG, "handleMessage ended")
        }
    }

    private var listener: RequestReceiver? = null
    private val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()
    private const val KEEP_ALIVE_TIME = 1L
    private val TIME_UNIT = TimeUnit.SECONDS

    private val executorQueue: BlockingQueue<Runnable> = LinkedBlockingQueue<Runnable>()

    private val networkingThreadPool: ThreadPoolExecutor = ThreadPoolExecutor(
        NUMBER_OF_CORES,
        NUMBER_OF_CORES,
        KEEP_ALIVE_TIME,
        TIME_UNIT,
        executorQueue
    )

    fun setListener(listener: RequestReceiver) {
        this.listener = listener
    }

    fun sendPostRequest(to: URL, data: PostData) {
        Log.d(TAG, "sendPostRequest started")
        val postTaskRunnable: Runnable =
            PostTaskBuilder()
                .withURL(to)
                .withData(data)
                .getRunnable()
        networkingThreadPool.execute(postTaskRunnable)
        Log.d(TAG, "sendPostRequest ended")
    }

    fun executeCyclic(func: () -> Unit, delaySeconds: Long) {
        Log.d(TAG, "executeCyclic started")
        val funcWrapper = object : Runnable {
            override fun run() {
                func()
                handler.postDelayed(this, delaySeconds * 1000)
            }
        }
        handler.postDelayed(funcWrapper, delaySeconds * 1000)
        Log.d(TAG, "executeCyclic ended")
    }

    fun sendGetRequest(to: URL) {
        Log.d(TAG, "sendGetRequest started")
        val getTaskRunnable: Runnable =
            GetTaskBuilder().withURL(to).getRunnable()
        networkingThreadPool.execute(getTaskRunnable)
        Log.d(TAG, "sendGetRequest ended")
    }

    fun handleGetResult(result: RESULT, data: String) {
        Log.d(TAG, "handleGetResult started")
        handler.obtainMessage(result.value, data)?.apply {
            sendToTarget()
        }
        Log.d(TAG, "handleGetResult ended")
    }

    fun handlePostResult(result: RESULT) {
        Log.d(TAG, "handlePostResult started")
        handler.obtainMessage(result.value)?.apply {
            sendToTarget()
        }
        Log.d(TAG, "handlePostResult ended")
    }
}