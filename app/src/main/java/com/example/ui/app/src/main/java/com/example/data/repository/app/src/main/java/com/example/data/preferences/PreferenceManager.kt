package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.TemperatureUnit

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    var temperatureUnit: TemperatureUnit
        get() {
            val saved = prefs.getString(
                KEY_TEMPERATURE_UNIT,
                TemperatureUnit.CELSIUS.name
            )

            return try {
                TemperatureUnit.valueOf(
                    saved ?: TemperatureUnit.CELSIUS.name
                )
            } catch (e: Exception) {
                TemperatureUnit.CELSIUS
            }
        }
        set(value) {
            prefs.edit()
                .putString(KEY_TEMPERATURE_UNIT, value.name)
                .apply()
        }

    var lastLocationName: String
        get() = prefs.getString(
            KEY_LAST_LOCATION_NAME,
            "Current Location"
        ) ?: "Current Location"

        set(value) {
            prefs.edit()
                .putString(KEY_LAST_LOCATION_NAME, value)
                .apply()
        }

    var lastLatitude: Double
        get() = Double.fromBits(
            prefs.getLong(
                KEY_LAST_LATITUDE,
                0.0.toBits()
            )
        )

        set(value) {
            prefs.edit()
                .putLong(KEY_LAST_LATITUDE, value.toBits())
                .apply()
        }

    var lastLongitude: Double
        get() = Double.fromBits(
            prefs.getLong(
                KEY_LAST_LONGITUDE,
                0.0.toBits()
            )
        )

        set(value) {
            prefs.edit()
                .putLong(KEY_LAST_LONGITUDE, value.toBits())
                .apply()
        }

    var hasSavedLocation: Boolean
        get() = prefs.getBoolean(
            KEY_HAS_SAVED_LOCATION,
            false
        )

        set(value) {
            prefs.edit()
                .putBoolean(KEY_HAS_SAVED_LOCATION, value)
                .apply()
        }

    companion object {
        private const val PREFS_NAME = "skynet_weather_prefs"

        private const val KEY_TEMPERATURE_UNIT =
            "key_temp_unit"

        private const val KEY_LAST_LOCATION_NAME =
            "key_last_location_name"

        private const val KEY_LAST_LATITUDE =
            "key_last_lat"

        private const val KEY_LAST_LONGITUDE =
            "key_last_lon"

        private const val KEY_HAS_SAVED_LOCATION =
            "key_has_saved_location"
    }
}
