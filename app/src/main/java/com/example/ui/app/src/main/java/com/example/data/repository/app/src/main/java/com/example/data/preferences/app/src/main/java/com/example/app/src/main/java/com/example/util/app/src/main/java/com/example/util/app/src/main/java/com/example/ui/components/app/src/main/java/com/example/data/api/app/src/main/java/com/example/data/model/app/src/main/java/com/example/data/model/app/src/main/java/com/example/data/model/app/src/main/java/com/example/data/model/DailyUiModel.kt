package com.example.data.model

data class DailyUiModel(
    val dayDisplay: String,
    val dateDisplay: String,
    val condition: String,
    val weatherCode: Int,
    val tempHighC: Double,
    val tempLowC: Double
)
