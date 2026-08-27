package com.example.util

import com.example.data.model.TemperatureUnit
import java.util.Locale
import kotlin.math.roundToInt

object UnitFormatter {

    fun formatTemperature(
        tempC: Double,
        unit: TemperatureUnit
    ): String {
        val temp = if (unit == TemperatureUnit.FAHRENHEIT) {
            (tempC * 9.0 / 5.0) + 32.0
        } else {
            tempC
        }

        return "${temp.roundToInt()}°${if (unit == TemperatureUnit.FAHRENHEIT) "F" else "C"}"
    }

    fun formatTemperatureValue(
        tempC: Double,
        unit: TemperatureUnit
    ): String {
        val temp = if (unit == TemperatureUnit.FAHRENHEIT) {
            (tempC * 9.0 / 5.0) + 32.0
        } else {
            tempC
        }

        return "${temp.roundToInt()}°"
    }

    fun formatWindSpeed(
        speedKmh: Double,
        unit: TemperatureUnit
    ): String {
        return if (unit == TemperatureUnit.FAHRENHEIT) {
            val mph = speedKmh * 0.621371
            String.format(Locale.US, "%.1f mph", mph)
        } else {
            String.format(Locale.US, "%.1f km/h", speedKmh)
        }
    }

    fun formatPressure(pressureHpa: Double): String {
        return "${pressureHpa.roundToInt()} hPa"
    }

    fun formatHumidity(humidityPercent: Int): String {
        return "$humidityPercent%"
    }
}
