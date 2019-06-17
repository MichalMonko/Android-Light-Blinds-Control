package com.example.smarthomecontrol

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class SettingsActivity : AppCompatActivity() {
    private lateinit var applyButton: Button
    private lateinit var hostEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        applyButton = findViewById(R.id.settings_accept_button)
        hostEditText = findViewById(R.id.settings_host_ip_edit_text)

        hostEditText.setText(intent.getStringExtra(HOST_IP_KEY))

        applyButton.setOnClickListener {
            val returnIntent = Intent()
            returnIntent.putExtra(HOST_IP_KEY, hostEditText.text.toString())
            setResult(RESULT_OK,returnIntent)
            finish()
        }
    }

    override fun onBackPressed() {
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED,returnIntent)
        finish()
        super.onBackPressed()
    }

}
