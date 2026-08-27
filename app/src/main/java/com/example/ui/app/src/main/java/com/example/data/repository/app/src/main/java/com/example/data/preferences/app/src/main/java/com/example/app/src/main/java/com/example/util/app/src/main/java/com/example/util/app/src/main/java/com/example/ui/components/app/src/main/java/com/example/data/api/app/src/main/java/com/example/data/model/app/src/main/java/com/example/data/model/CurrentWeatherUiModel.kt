package com.example.data.model

data class CurrentWeatherUiModel(
    val locationName: String,
    val temperatureC: Double,
    val feelsLikeC: Double,
    val condition: String,
    val weatherCode: Int
)
