package com.example.smarthomecontrol.data_classes

import org.json.JSONObject

data class PostData(val r: Int, val g: Int, val b: Int, val shutterPosition: Int?, val manualControl: Boolean) {
    val jsonRepresentation: String
        get() {
            val jsonObject = JSONObject()
            jsonObject.put("red",r)
            jsonObject.put("green",g)
            jsonObject.put("blue",b)
            jsonObject.put("shutterPosition",shutterPosition)
            jsonObject.put("manualControl",manualControl)
            return jsonObject.toString()
        }
}