package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.WeatherScreen
import com.example.ui.theme.SkyNetBlack
import com.example.ui.theme.SkyNetWeatherTheme
import com.example.util.LocationHelper
import com.example.viewmodel.WeatherViewModel
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {

    private val weatherViewModel: WeatherViewModel by viewModels()

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                weatherViewModel.requestAutomaticLocation(this)
            } else {
                weatherViewModel.requestAutomaticLocation(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        try {
            MobileAds.initialize(this) {}
        } catch (e: Exception) {
            // Graceful fallback if Google Play services ads are unavailable
        }

        if (LocationHelper.hasLocationPermission(this)) {
            weatherViewModel.requestAutomaticLocation(this)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        setContent {
            SkyNetWeatherTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SkyNetBlack
                ) {

                    WeatherScreen(
                        viewModel = weatherViewModel,
                        onRequireLocationPermission = {

                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}
