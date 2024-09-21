package com.testing.sampleweatherproject.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel


@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var cityName by remember { mutableStateOf("") }
    val lastCity by viewModel.lastCity.collectAsState()

    LaunchedEffect(lastCity) {
        if (lastCity.isNotEmpty()) {
            cityName = lastCity
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = cityName,
            onValueChange = { cityName = it },
            label = { Text("Enter US city name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.getWeather(cityName) }) {
            Text("Get Weather")
        }
        Spacer(modifier = Modifier.height(16.dp))
        when {
            state.isLoading -> CircularProgressIndicator()
            state.error != null -> Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            state.weatherData != null -> {
                Text("City: ${state.weatherData!!.name}")
                Text("Temperature: ${state.weatherData!!.main.temp}°C")
                Text("Description: ${state.weatherData!!.weather.firstOrNull()?.description}")
            }
        }
    }
}