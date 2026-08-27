package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.SkyNetPurple
import com.example.ui.theme.SkyNetPurpleMuted

object WeatherCodeMapper {

    fun getConditionDescription(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45 -> "Foggy"
            48 -> "Depositing Rime Fog"
            51 -> "Light Drizzle"
            53 -> "Moderate Drizzle"
            55 -> "Dense Drizzle"
            56, 57 -> "Freezing Drizzle"
            61 -> "Slight Rain"
            63 -> "Moderate Rain"
            65 -> "Heavy Rain"
            66, 67 -> "Freezing Rain"
            71 -> "Slight Snow Fall"
            73 -> "Moderate Snow Fall"
            75 -> "Heavy Snow Fall"
            77 -> "Snow Grains"
            80 -> "Slight Rain Showers"
            81 -> "Moderate Rain Showers"
            82 -> "Violent Rain Showers"
            85, 86 -> "Snow Showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with Hail"
            else -> "Partly Cloudy"
        }
    }

    fun getWeatherIcon(code: Int): ImageVector {
        return when (code) {
            0 -> Icons.Default.WbSunny
            1, 2 -> Icons.Default.WbSunny
            3 -> Icons.Default.WbCloudy
            45, 48 -> Icons.Default.Air
            51, 53, 55, 56, 57 -> Icons.Default.Grain
            61, 63, 65, 66, 67, 80, 81, 82 ->
                Icons.Default.WaterDrop
            71, 73, 75, 77, 85, 86 ->
                Icons.Default.AcUnit
            95, 96, 99 ->
                Icons.Default.FlashOn
            else -> Icons.Default.WbCloudy
        }
    }

    fun getWeatherIconTint(code: Int): Color {
        return when (code) {
            0, 1 -> Color(0xFFFFB74D)
            2, 3 -> Color(0xFFB0BEC5)
            45, 48 -> Color(0xFF90A4AE)
            51, 53, 55, 61, 63, 65, 80, 81, 82 ->
                Color(0xFF4FC3F7)
            71, 73, 75, 77, 85, 86 ->
                Color(0xFF80DEEA)
            95, 96, 99 ->
                SkyNetPurple
            else ->
                SkyNetPurpleMuted
        }
    }
}
