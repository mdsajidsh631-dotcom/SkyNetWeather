package com.example.data.repository

import com.example.data.api.NetworkClient
import com.example.data.api.WeatherApiService
import com.example.data.model.CurrentWeatherUiModel
import com.example.data.model.DailyUiModel
import com.example.data.model.HourlyUiModel
import com.example.data.model.SearchResultUiModel
import com.example.data.model.WeatherDetailsUiModel
import com.example.util.WeatherCodeMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

data class WeatherBundle(
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeatherUiModel,
    val hourly: List<HourlyUiModel>,
    val daily: List<DailyUiModel>,
    val details: WeatherDetailsUiModel,
    val fetchedTimestamp: Long = System.currentTimeMillis()
)

sealed class WeatherResult<out T> {
    data class Success<T>(val data: T) : WeatherResult<T>()

    data class Error(
        val message: String,
        val isNetworkError: Boolean = false
    ) : WeatherResult<Nothing>()
}

class WeatherRepository(
    private val apiService: WeatherApiService = NetworkClient.weatherApiService
) {
    private var cachedBundle: WeatherBundle? = null
    private var lastFetchTime = 0L

    suspend fun fetchWeather(
        latitude: Double,
        longitude: Double,
        locationName: String,
        forceRefresh: Boolean = false
    ): WeatherResult<WeatherBundle> = withContext(Dispatchers.IO) {

        val currentTime = System.currentTimeMillis()

        if (
            !forceRefresh &&
            cachedBundle != null &&
            cachedBundle?.latitude == latitude &&
            cachedBundle?.longitude == longitude &&
            currentTime - lastFetchTime < 30_000
        ) {
            return@withContext WeatherResult.Success(cachedBundle!!)
        }

        try {
            val response = apiService.getForecast(
                latitude = latitude,
                longitude = longitude
            )

            val currentDto = response.current
                ?: return@withContext WeatherResult.Error(
                    "No current weather data available."
                )

            val tempC = currentDto.temperature2m ?: 0.0
            val feelsLikeC = currentDto.apparentTemperature ?: tempC
            val weatherCode = currentDto.weatherCode ?: 0

            val conditionDesc =
                WeatherCodeMapper.getConditionDescription(weatherCode)

            val currentWeather = CurrentWeatherUiModel(
                locationName = locationName,
                temperatureC = tempC,
                feelsLikeC = feelsLikeC,
                condition = conditionDesc,
                weatherCode = weatherCode
            )

            val hourlyList = mutableListOf<HourlyUiModel>()
            val hourlyDto = response.hourly

            if (
                hourlyDto?.time != null &&
                hourlyDto.temperature2m != null
            ) {
                val count = minOf(
                    hourlyDto.time.size,
                    hourlyDto.temperature2m.size,
                    24
                )

                val isoFormat =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)

                val displayTimeFormat =
                    SimpleDateFormat("h a", Locale.US)

                for (i in 0 until count) {

                    val rawTime =
                        hourlyDto.time.getOrNull(i) ?: ""

                    val hTemp =
                        hourlyDto.temperature2m.getOrNull(i) ?: 0.0

                    val hCode =
                        hourlyDto.weatherCode?.getOrNull(i) ?: 0

                    val hCondition =
                        WeatherCodeMapper.getConditionDescription(hCode)

                    val formattedTime = try {
                        val date = isoFormat.parse(rawTime)

                        if (date != null) {
                            displayTimeFormat.format(date)
                        } else {
                            rawTime
                        }
                    } catch (e: Exception) {
                        rawTime
                    }

                    hourlyList.add(
                        HourlyUiModel(
                            timeDisplay =
                                if (i == 0) "Now" else formattedTime,
                            temperatureC = hTemp,
                            condition = hCondition,
                            weatherCode = hCode,
                            isCurrent = i == 0
                        )
                    )
                }
            }

            val dailyList = mutableListOf<DailyUiModel>()
            val dailyDto = response.daily

            if (
                dailyDto?.time != null &&
                dailyDto.temperature2mMax != null &&
                dailyDto.temperature2mMin != null
            ) {
                val count = minOf(
                    dailyDto.time.size,
                    dailyDto.temperature2mMax.size,
                    dailyDto.temperature2mMin.size,
                    7
                )

                val dateParser =
                    SimpleDateFormat("yyyy-MM-dd", Locale.US)

                val dayNameFormat =
                    SimpleDateFormat("EEEE", Locale.US)

                val shortDateFormat =
                    SimpleDateFormat("MMM d", Locale.US)

                for (i in 0 until count) {

                    val rawDate =
                        dailyDto.time.getOrNull(i) ?: ""

                    val maxTemp =
                        dailyDto.temperature2mMax.getOrNull(i) ?: 0.0

                    val minTemp =
                        dailyDto.temperature2mMin.getOrNull(i) ?: 0.0

                    val dCode =
                        dailyDto.weatherCode?.getOrNull(i) ?: 0

                    val dCondition =
                        WeatherCodeMapper.getConditionDescription(dCode)

                    var dayName = "Day $i"
                    var dateString = rawDate

                    try {
                        val parsedDate = dateParser.parse(rawDate)

                        if (parsedDate != null) {
                            dayName =
                                if (i == 0) {
                                    "Today"
                                } else {
                                    dayNameFormat.format(parsedDate)
                                }

                            dateString =
                                shortDateFormat.format(parsedDate)
                        }
                    } catch (e: Exception) {
                        // Keep default values
                    }

                    dailyList.add(
                        DailyUiModel(
                            dayDisplay = dayName,
                            dateDisplay = dateString,
                            condition = dCondition,
                            weatherCode = dCode,
                            tempHighC = maxTemp,
                            tempLowC = minTemp
                        )
                    )
                }
            }

            val details = WeatherDetailsUiModel(
                humidityPercent =
                    currentDto.relativeHumidity2m ?: 0,

                windSpeedKmh =
                    currentDto.windSpeed10m ?: 0.0,

                atmosphericPressureHpa =
                    currentDto.surfacePressure ?: 1013.25
            )

            val bundle = WeatherBundle(
                locationName = locationName,
                latitude = latitude,
                longitude = longitude,
                current = currentWeather,
                hourly = hourlyList,
                daily = dailyList,
                details = details,
                fetchedTimestamp = currentTime
            )

            cachedBundle = bundle
            lastFetchTime = currentTime

            WeatherResult.Success(bundle)

        } catch (e: IOException) {

            WeatherResult.Error(
                message =
                    "Unable to connect to weather server. Please check your internet connection.",
                isNetworkError = true
            )

        } catch (e: Exception) {

            WeatherResult.Error(
                message =
                    e.localizedMessage
                        ?: "Unexpected error fetching weather data.",
                isNetworkError = false
            )
        }
    }

    suspend fun searchCities(
        query: String
    ): WeatherResult<List<SearchResultUiModel>> =
        withContext(Dispatchers.IO) {

            if (query.trim().length < 2) {
                return@withContext WeatherResult.Success(emptyList())
            }

            try {

                val response =
                    apiService.searchCity(query = query.trim())

                val items =
                    response.results?.map { dto ->

                        SearchResultUiModel(
                            name = dto.name ?: "Unknown",
                            country = dto.country,
                            admin1 = dto.admin1,
                            latitude = dto.latitude ?: 0.0,
                            longitude = dto.longitude ?: 0.0
                        )

                    } ?: emptyList()

                WeatherResult.Success(items)

            } catch (e: IOException) {

                WeatherResult.Error(
                    "Network error while searching cities.",
                    isNetworkError = true
                )

            } catch (e: Exception) {

                WeatherResult.Error(
                    "Failed to search cities. Please try again."
                )
            }
        }
}
