package com.example.data.model

data class HourlyUiModel(
    val timeDisplay: String,
    val temperatureC: Double,
    val condition: String,
    val weatherCode: Int,
    val isCurrent: Boolean = false
)
