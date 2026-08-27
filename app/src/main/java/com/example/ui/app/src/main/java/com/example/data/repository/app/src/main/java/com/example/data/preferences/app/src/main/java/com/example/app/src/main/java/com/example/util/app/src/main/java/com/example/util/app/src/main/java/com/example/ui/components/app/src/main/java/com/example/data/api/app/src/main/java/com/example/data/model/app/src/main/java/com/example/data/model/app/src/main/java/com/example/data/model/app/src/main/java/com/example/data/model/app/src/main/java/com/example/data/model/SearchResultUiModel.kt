package com.example.data.model

data class SearchResultUiModel(
    val name: String,
    val country: String?,
    val admin1: String?,
    val latitude: Double,
    val longitude: Double
) {
    val fullDisplayName: String
        get() {
            return listOfNotNull(
                name.takeIf { it.isNotBlank() },
                admin1?.takeIf { it.isNotBlank() },
                country?.takeIf { it.isNotBlank() }
            ).joinToString(", ")
        }
}
