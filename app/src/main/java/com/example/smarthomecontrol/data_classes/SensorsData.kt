package com.example.smarthomecontrol.data_classes

import org.json.JSONObject

object SensorsDataJsonParser {
    fun parse(data: String) : SensorsData {
        val jsonObject = JSONObject(data)
        with(jsonObject)
        {
            val red = getInt("red")
            val green = getInt("green")
            val blue = getInt("blue")
            val temperature = getDouble("temperature")
            val shutterPosition = getInt("shutterPosition")
            return SensorsData(
                temperature, red, green, blue,
                shutterPosition)
        }
    }
}

data class SensorsData(
    val temperature: Double,
    val R: Int,
    val G: Int,
    val B: Int,
    val shutterProgress: Int
)
