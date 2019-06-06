package com.example.smarthomecontrol

import android.annotation.SuppressLint
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.smarthomecontrol.data_classes.PostData
import com.example.smarthomecontrol.data_classes.SensorsData
import com.example.smarthomecontrol.data_classes.SensorsDataJsonParser
import com.example.smarthomecontrol.networking.ErrorType
import com.example.smarthomecontrol.networking.RequestExecutor
import com.example.smarthomecontrol.networking.RequestExecutor.sendPostRequest
import com.example.smarthomecontrol.networking.RequestReceiver
import com.example.smarthomecontrol.networking.RequestType
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL

const val DELAY_SECONDS = 10L
const val TAG = "MAIN_ACTIVITY"

class MainActivity : AppCompatActivity(), RequestReceiver {
    private lateinit var Rslider: SeekBar
    private lateinit var Gslider: SeekBar
    private lateinit var Bslider: SeekBar

    private lateinit var Rvalue: TextView
    private lateinit var Gvalue: TextView
    private lateinit var Bvalue: TextView

    private lateinit var colorResult: TextView

    private lateinit var shutterControlSlider: SeekBar
    private lateinit var manualControlSwitch: Switch
    private lateinit var applyButton: Button

    private var manualControl = false

    private val getUrl: URL = URL("http://192.168.1.234:8000/data/stateData")
    private val postUrl: URL = URL("http://192.168.1.234:8000/data/stateData")

    private inner class SliderChangeListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            Log.d(TAG, "onProgressChanged called")
            val textView = when (seekBar) {
                Rslider -> Rvalue
                Gslider -> Gvalue
                Bslider -> Bvalue
                shutterControlSlider -> shutterPositionText
                else -> throw IllegalArgumentException("Undefined Seekbar passed to change listener")
            }
            textView.text = progress.toString()

            if (seekBar in listOf(Rslider, Gslider, Bslider)) {

                var Rhex = Rslider.progress.toString(16)
                if (Rhex.length < 2) Rhex = "0$Rhex"
                var Ghex = Gslider.progress.toString(16)
                if (Ghex.length < 2) Ghex = "0$Ghex"
                var Bhex = Bslider.progress.toString(16)
                if (Bhex.length < 2) Bhex = "0$Bhex"

                val newColor = "#$Rhex$Ghex$Bhex"
                colorResult.setBackgroundColor(Color.parseColor(newColor))
            }
            else
            {
                textView.append(" %")
            }
            Log.d(TAG, "onProgressChanged ended")
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate called")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Rslider = findViewById(R.id.Rslider)
        Gslider = findViewById(R.id.Gslider)
        Bslider = findViewById(R.id.Bslider)

        colorResult = findViewById(R.id.colorResult)

        val sliderListener = SliderChangeListener()
        Rslider.setOnSeekBarChangeListener(sliderListener)
        Gslider.setOnSeekBarChangeListener(sliderListener)
        Bslider.setOnSeekBarChangeListener(sliderListener)

        Rvalue = findViewById(R.id.RvalueTextView)
        Gvalue = findViewById(R.id.GTextView)
        Bvalue = findViewById(R.id.Bvalue)

        shutterControlSlider = findViewById(R.id.shutterSlider)
        manualControlSwitch = findViewById(R.id.manualSwitch)

        manualControlSwitch.setOnCheckedChangeListener { _, isChecked ->
            enableManualControl(isChecked)
        }


        shutterControlSlider.setOnSeekBarChangeListener(sliderListener)
        shutterControlSlider.max = 100

        applyButton = findViewById(R.id.applyButton)
        applyButton.setOnClickListener {
            sendPostRequest()
        }

        enableManualControl(false)

        RequestExecutor.setListener(this)
        RequestExecutor.executeCyclic(this::sendGetRequest, DELAY_SECONDS)
        Log.d(TAG, "onCreate ended")
    }


    override fun onSuccess(requestType: RequestType, data: String?) {
        Log.d(TAG, "onSuccess started")
        when (requestType) {
            RequestType.GET -> data?.let { applyChanges(parseData(data)) }
            RequestType.POST -> Toast.makeText(this, "Data sent successfully", Toast.LENGTH_LONG).show()
        }
        Log.d(TAG, "onSuccess ended")
    }

    private fun parseData(data: String): SensorsData {
        Log.d(TAG, "parseData started, returning data")
        return SensorsDataJsonParser.parse(data)
    }

    @SuppressLint("SetTextI18n")
    private fun applyChanges(sensorData: SensorsData) {
        Log.d(TAG, "applyChanges() started")
        if (!manualControl) {
            Rslider.progress = sensorData.R
            Gslider.progress = sensorData.G
            Bslider.progress = sensorData.B

            Rslider.refreshDrawableState()
            Bslider.refreshDrawableState()
            Gslider.refreshDrawableState()

            Rvalue.text = sensorData.R.toString()
            Gvalue.text = sensorData.G.toString()
            Bvalue.text = sensorData.B.toString()
            shutterControlSlider.progress = sensorData.shutterProgress
            shutterControlSlider.refreshDrawableState()
            shutterPositionText.text = "${sensorData.shutterProgress} %"
        }
        temperatureTextView.text = sensorData.temperature.toString()

        Log.d(TAG, "applyChanges() ended")
    }

    override fun onError(requestType: RequestType, error: ErrorType) {
        Log.d(TAG, "onError() started")
        val infoPrompt = when (requestType) {
            RequestType.GET -> "Cannot get data from server : "
            RequestType.POST -> "Cannot send data to server : "
        }
        val errorPrompt = when (error) {
            ErrorType.CONNECTION_ERROR -> "Cannot connect to server"
            ErrorType.OTHER_ERROR -> "Other error occurred"
        }

        Toast.makeText(this, infoPrompt + errorPrompt, Toast.LENGTH_LONG).show()
        Log.d(TAG, "onError() ended")
    }


    private fun enableManualControl(enabled: Boolean) {
        Log.d(TAG, "enableManualControl started")
        Rslider.isEnabled = enabled
        Gslider.isEnabled = enabled
        Bslider.isEnabled = enabled
        shutterControlSlider.isEnabled = enabled
        applyButton.isEnabled = enabled
        if (enabled != manualControl) {
            manualControl = enabled
        }
        Log.d(TAG, "enableManualControl ended")
    }

    private fun sendGetRequest() {
        Log.d(TAG, "sendGetRequest started")
        RequestExecutor.sendGetRequest(getUrl)
        Log.d(TAG, "sendGetRequest ended")
    }

    private fun sendPostRequest() {
        Log.d(TAG, "sendData started")
        val data = PostData(
            Rslider.progress,
            Gslider.progress,
            Bslider.progress,
            shutterControlSlider.progress,
            manualControl
        )
        sendPostRequest(postUrl, data)
        Log.d(TAG, "sendData ended")
    }
}
