package com.example.smarthomecontrol.data_classes

import android.util.JsonWriter
import org.json.JSONObject
import org.json.JSONStringer

data class PostData(val r: Int, val g: Int, val b: Int, val shutterPosition: Int?, val manualControl: Boolean) {
    val jsonRepresentation: String
        get() {
            val jsonObject = JSONObject()
            jsonObject.put("red",r)
            jsonObject.put("green",g)
            jsonObject.put("blue",b)
            jsonObject.put("shutterState",shutterPosition)
            jsonObject.put("manualControl",manualControl)
            return jsonObject.toString()
        }
}