package com.example.smarthomecontrol

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.example.smarthomecontrol.data_classes.PostData
import com.example.smarthomecontrol.data_classes.SensorsData
import com.example.smarthomecontrol.data_classes.SensorsDataJsonParser
import com.example.smarthomecontrol.networking.ErrorType
import com.example.smarthomecontrol.networking.RequestExecutor
import com.example.smarthomecontrol.networking.RequestExecutor.sendGetRequest
import com.example.smarthomecontrol.networking.RequestExecutor.sendPostRequest
import com.example.smarthomecontrol.networking.RequestReceiver
import com.example.smarthomecontrol.networking.RequestType
import kotlinx.android.synthetic.main.activity_main.*
import java.net.MalformedURLException
import java.net.URL

const val DELAY_SECONDS = 10L
const val SET_HOST_IP_CODE = 100
const val HOST_IP_KEY = "HostIP"
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

    private var host = "http://192.168.1.234:8000"
    private var apiRoute = "/data/stateData"

    private var getUrl: URL = URL("http://192.168.1.234:8000/data/stateData")
    private var postUrl: URL = URL("http://192.168.1.234:8000/data/stateData")

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
            } else {
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

        val savedHost = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .getString(HOST_IP_KEY, null)

        savedHost?.let {
            updateUrl(savedHost)
        }


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

    private fun updateUrl(host: String) {
        try {
            this.host = host
            val newGetUrl = URL("$host$apiRoute")
            val newPostUrl = URL("$host$apiRoute")

            getUrl = newGetUrl
            postUrl = newPostUrl

            PreferenceManager
                .getDefaultSharedPreferences(applicationContext)
                .edit()
                .putString(HOST_IP_KEY, host)
                .apply()
        } catch (ex: MalformedURLException) {
            Toast.makeText(this, "Provided URL is invalid no changes were made", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onSuccess(requestType: RequestType, data: String?) {
        Log.d(TAG, "onSuccess started")
        when (requestType) {
            RequestType.GET -> data?.let { applyChanges(parseData(data)) }
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
        temperatureTextView.text = "${sensorData.temperature} \u00B0C"

        Log.d(TAG, "applyChanges() ended")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.settingsButton -> {
                val intent = Intent(this,SettingsActivity::class.java)
                intent.putExtra(HOST_IP_KEY, host)
                startActivityForResult(intent, SET_HOST_IP_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SET_HOST_IP_CODE -> {
                    host = data.getStringExtra(HOST_IP_KEY)
                    updateUrl(host)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onError(requestType: RequestType, error: ErrorType) {
        Log.d(TAG, "onError() started")
        val infoPrompt = when (requestType) {
            RequestType.GET -> "Cannot get data from server : "
            RequestType.POST -> "Cannot send data to server : "
        }
        val errorPrompt = when (error) {
            ErrorType.CONNECTION_ERROR -> "Connection Error"
            ErrorType.OTHER_ERROR -> "Other error occurred"
        }

        shutterPositionText.text = errorPrompt
        temperatureTextView.text = errorPrompt
        //Toast.makeText(this, infoPrompt + errorPrompt, Toast.LENGTH_LONG).show()
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
