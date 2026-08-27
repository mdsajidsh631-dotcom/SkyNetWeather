package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.*
import com.example.ui.components.AdMobBanner
import com.example.ui.theme.*
import com.example.util.UnitFormatter
import com.example.util.WeatherCodeMapper
import com.example.viewmodel.WeatherErrorType
import com.example.viewmodel.WeatherUiState
import com.example.viewmodel.WeatherViewModel

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    onRequireLocationPermission: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize().background(SkyNetBlack),
        containerColor = SkyNetBlack,
        bottomBar = {
            AdMobBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SkyNetBlack)
                    .border(1.dp, SkyNetBorder.copy(alpha = 0.5f))
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SkyNetBlack)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("weather_scroll_column"),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    WeatherTopHeader(
                        uiState = uiState,
                        onToggleSearch = { viewModel.toggleSearchOpen() },
                        onToggleUnit = { viewModel.toggleTemperatureUnit() },
                        onRequestLocation = { onRequireLocationPermission() },
                        onRefresh = { viewModel.refreshWeather() }
                    )
                }

                if (uiState.isSearchOpen) {
                    item {
                        CitySearchSection(
                            query = uiState.searchQuery,
                            isSearching = uiState.isSearching,
                            results = uiState.searchResults,
                            onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                            onCitySelected = { viewModel.selectCity(it) },
                            onClose = { viewModel.toggleSearchOpen(false) }
                        )
                    }
                }

                if (uiState.errorType != WeatherErrorType.NONE &&
                    uiState.errorMessage != null
                ) {
                    item {
                        ErrorNotificationCard(
                            errorType = uiState.errorType,
                            errorMessage = uiState.errorMessage ?: "",
                            onRetry = { viewModel.retry() },
                            onDismiss = { viewModel.dismissError() },
                            onRequestPermission =
                                if (uiState.errorType == WeatherErrorType.LOCATION)
                                    onRequireLocationPermission
                                else null
                        )
                    }
                }

                if (uiState.isLoading && uiState.currentWeather == null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = SkyNetPurple,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("initial_loading_indicator")
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Loading weather data...",
                                    color = SkyNetPurpleMuted,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                uiState.currentWeather?.let { current ->
                    item {
                        CurrentWeatherCard(
                            current = current,
                            unit = uiState.temperatureUnit,
                            isRefreshing = uiState.isRefreshing
                        )
                    }

                    if (uiState.hourlyForecast.isNotEmpty()) {
                        item {
                            HourlyForecastSection(
                                hourlyList = uiState.hourlyForecast,
                                unit = uiState.temperatureUnit
                            )
                        }
                    }

                    if (uiState.dailyForecast.isNotEmpty()) {
                        item {
                            DailyForecastSection(
                                dailyList = uiState.dailyForecast,
                                unit = uiState.temperatureUnit
                            )
                        }
                    }

                    uiState.weatherDetails?.let { details ->
                        item {
                            EssentialWeatherDetailsSection(
                                details = details,
                                unit = uiState.temperatureUnit
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherTopHeader(
    uiState: WeatherUiState,
    onToggleSearch: () -> Unit,
    onToggleUnit: () -> Unit,
    onRequestLocation: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SkyNetBlack)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.skynet_logo),
                contentDescription = stringResource(R.string.app_logo_description),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(
                        1.5.dp,
                        SkyNetPurple.copy(alpha = 0.6f),
                        CircleShape
                    )
                    .testTag("skynet_header_logo"),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SkyNetWhite
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = SkyNetPurple,
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    Text(
                        text = uiState.currentLocationName,
                        fontSize = 12.sp,
                        color = SkyNetPurpleMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onRequestLocation,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("location_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = stringResource(R.string.use_my_location),
                    tint = if (uiState.isUsingGpsLocation)
                        SkyNetPurple
                    else Color(0xFF888888)
                )
            }

            IconButton(
                onClick = onToggleSearch,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("search_toggle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search city",
                    tint = if (uiState.isSearchOpen)
                        SkyNetPurple
                    else Color(0xFF888888)
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SkyNetDarkCard,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    SkyNetBorder
                ),
                modifier = Modifier
                    .clickable { onToggleUnit() }
                    .testTag("unit_toggle_button")
            ) {
                Text(
                    text = if (
                        uiState.temperatureUnit == TemperatureUnit.CELSIUS
                    ) "°C" else "°F",
                    color = SkyNetPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 6.dp
                    )
                )
            }

            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("refresh_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(
                        R.string.refresh_button_desc
                    ),
                    tint = SkyNetWhite
                )
            }
        }
    }
}

@Composable
fun CitySearchSection(
    query: String,
    isSearching: Boolean,
    results: List<SearchResultUiModel>,
    onQueryChanged: (String) -> Unit,
    onCitySelected: (SearchResultUiModel) -> Unit,
    onClose: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SkyNetBlack)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("city_search_input"),
            placeholder = {
                Text(
                    text = stringResource(
                        R.string.search_city_placeholder
                    ),
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = SkyNetPurple
                )
            },
            trailingIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close search",
                        tint = Color(0xFF888888)
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SkyNetDarkCard,
                unfocusedContainerColor = SkyNetDarkCard,
                focusedBorderColor = SkyNetPurple,
                unfocusedBorderColor = SkyNetBorder,
                focusedTextColor = SkyNetWhite,
                unfocusedTextColor = SkyNetWhite
            ),
            shape = RoundedCornerShape(12.dp)
        )

        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = SkyNetPurple,
                    strokeWidth = 2.dp
                )
            }
        }

        if (results.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = SkyNetDarkCard,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    SkyNetBorder
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    results.forEachIndexed { index, city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    keyboardController?.hide()
                                    onCitySelected(city)
                                }
                                .padding(
                                    horizontal = 14.dp,
                                    vertical = 12.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = SkyNetPurple,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = city.fullDisplayName,
                                color = SkyNetWhite,
                                fontSize = 14.sp
                            )
                        }

                        if (index < results.size - 1) {
                            HorizontalDivider(
                                color = SkyNetBorder.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorNotificationCard(
    errorType: WeatherErrorType,
    errorMessage: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onRequestPermission: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("error_card"),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1F1111),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFF5A2525)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = when (errorType) {
                            WeatherErrorType.NETWORK ->
                                stringResource(R.string.internet_error_msg)

                            WeatherErrorType.LOCATION ->
                                stringResource(R.string.location_denied_msg)

                            WeatherErrorType.API ->
                                stringResource(R.string.api_error_msg)

                            else -> errorMessage
                        },
                        color = Color(0xFFFFD2D2),
                        fontSize = 13.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (
                    errorType == WeatherErrorType.LOCATION &&
                    onRequestPermission != null
                ) {
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SkyNetPurple,
                            contentColor = SkyNetWhite
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            stringResource(R.string.grant_permission),
                            fontSize = 12.sp
                        )
                    }
                }

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B2020),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        stringResource(R.string.retry_button),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CurrentWeatherCard(
    current: CurrentWeatherUiModel,
    unit: TemperatureUnit,
    isRefreshing: Boolean
) {
    val icon = WeatherCodeMapper.getWeatherIcon(current.weatherCode)
    val iconTint = WeatherCodeMapper.getWeatherIconTint(
        current.weatherCode
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("current_weather_card"),
        shape = RoundedCornerShape(20.dp),
        color = SkyNetDarkCard,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            SkyNetBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    color = SkyNetPurple,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.End),
                    strokeWidth = 2.dp
                )
            }

            Text(
                text = current.locationName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = SkyNetWhite
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Icon(
                imageVector = icon,
                contentDescription = current.condition,
                tint = iconTint,
                modifier = Modifier
                    .size(76.dp)
                    .testTag("current_weather_icon")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = UnitFormatter.formatTemperature(
                    current.temperatureC,
                    unit
                ),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SkyNetWhite,
                    fontSize = 64.sp
                ),
                modifier = Modifier.testTag("current_temp_text")
            )

            Text(
                text = current.condition,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = SkyNetPurpleMuted,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.testTag("current_condition_text")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${
                    stringResource(R.string.feels_like_label)
                } ${
                    UnitFormatter.formatTemperature(
                        current.feelsLikeC,
                        unit
                    )
                }",
                fontSize = 13.sp,
                color = Color(0xFFAAAAAA),
                modifier = Modifier.testTag("feels_like_text")
            )
        }
    }
}

@Composable
fun HourlyForecastSection(
    hourlyList: List<HourlyUiModel>,
    unit: TemperatureUnit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.hourly_forecast_title),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SkyNetWhite
            ),
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 6.dp
            )
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hourly_forecast_row"),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(hourlyList) { item ->
                HourlyItemCard(
                    item = item,
                    unit = unit
                )
            }
        }
    }
}

@Composable
fun HourlyItemCard(
    item: HourlyUiModel,
    unit: TemperatureUnit
) {
    val icon = WeatherCodeMapper.getWeatherIcon(item.weatherCode)
    val iconTint = WeatherCodeMapper.getWeatherIconTint(
        item.weatherCode
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (item.isCurrent)
            Color(0xFF1E142B)
        else SkyNetDarkCard,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (item.isCurrent)
                SkyNetPurple.copy(alpha = 0.8f)
            else SkyNetBorder
        ),
        modifier = Modifier.width(74.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 12.dp,
                    horizontal = 4.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.timeDisplay,
                fontSize = 12.sp,
                fontWeight = if (item.isCurrent)
                    FontWeight.Bold
                else FontWeight.Normal,
                color = if (item.isCurrent)
                    SkyNetPurple
                else Color(0xFFAAAAAA)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Icon(
                imageVector = icon,
                contentDescription = item.condition,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = UnitFormatter.formatTemperatureValue(
                    item.temperatureC,
                    unit
                ),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SkyNetWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.condition,
                fontSize = 9.sp,
                color = Color(0xFF888888),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DailyForecastSection(
    dailyList: List<DailyUiModel>,
    unit: TemperatureUnit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
    ) {
        Text(
            text = stringResource(R.string.daily_forecast_title),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SkyNetWhite
            ),
            modifier = Modifier.padding(vertical = 6.dp)
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SkyNetDarkCard,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                SkyNetBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_forecast_card")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                dailyList.forEachIndexed { index, item ->
                    DailyItemRow(
                        item = item,
                        unit = unit
                    )

                    if (index < dailyList.size - 1) {
                        HorizontalDivider(
                            color = SkyNetBorder.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyItemRow(
    item: DailyUiModel,
    unit: TemperatureUnit
) {
    val icon = WeatherCodeMapper.getWeatherIcon(item.weatherCode)
    val iconTint = WeatherCodeMapper.getWeatherIconTint(
        item.weatherCode
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.width(90.dp)) {
            Text(
                text = item.dayDisplay,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = SkyNetWhite
            )

            Text(
                text = item.dateDisplay,
                fontSize = 11.sp,
                color = Color(0xFF888888)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = item.condition,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = item.condition,
                fontSize = 13.sp,
                color = SkyNetPurpleMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = UnitFormatter.formatTemperatureValue(
                    item.tempHighC,
                    unit
                ),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = SkyNetWhite
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = UnitFormatter.formatTemperatureValue(
                    item.tempLowC,
                    unit
                ),
                fontSize = 14.sp,
                color = Color(0xFF777777)
            )
        }
    }
}

@Composable
fun EssentialWeatherDetailsSection(
    details: WeatherDetailsUiModel,
    unit: TemperatureUnit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
    ) {
        Text(
            text = stringResource(R.string.weather_details_title),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SkyNetWhite
            ),
            modifier = Modifier.padding(vertical = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DetailMetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.WaterDrop,
                iconTint = Color(0xFF4FC3F7),
                title = stringResource(R.string.humidity_label),
                value = UnitFormatter.formatHumidity(
                    details.humidityPercent
                ),
                tag = "humidity_detail_card"
            )

            DetailMetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Air,
                iconTint = Color(0xFF81C784),
                title = stringResource(R.string.wind_speed_label),
                value = UnitFormatter.formatWindSpeed(
                    details.windSpeedKmh,
                    unit
                ),
                tag = "wind_detail_card"
            )

            DetailMetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Compress,
                iconTint = SkyNetPurple,
                title = stringResource(R.string.pressure_label),
                value = UnitFormatter.formatPressure(
                    details.atmosphericPressureHpa
                ),
                tag = "pressure_detail_card"
            )
        }
    }
}

@Composable
fun DetailMetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    tag: String
) {
    Surface(
        modifier = modifier.testTag(tag),
        shape = RoundedCornerShape(14.dp),
        color = SkyNetDarkCard,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            SkyNetBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                color = Color(0xFF888888),
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SkyNetWhite,
                textAlign = TextAlign.Center
            )
        }
    }
}
