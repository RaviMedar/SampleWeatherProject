package com.testing.sampleweatherproject.ui.home

import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.testing.sampleweatherproject.data.DataStore
import com.testing.sampleweatherproject.data.model.WeatherData
import com.testing.sampleweatherproject.domain.GetWeatherUseCase
import com.testing.sampleweatherproject.util.GeocoderManager
import com.testing.sampleweatherproject.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getWeatherUseCase: GetWeatherUseCase,
    private val dataStore: DataStore,
    private val locationManager: LocationManager,
    private val geocoderManager: GeocoderManager
) : ViewModel() {

    private val _state = MutableStateFlow(WeatherState())
    val state: StateFlow<WeatherState> = _state

    private val _lastCity = MutableStateFlow("")
    val lastCity: StateFlow<String> = _lastCity

    private val _lastLocation = MutableStateFlow("")
    val lastLocation: StateFlow<String> = _lastLocation

    private val _locationPermissionGranted = MutableStateFlow(false)
    val locationPermissionGranted: StateFlow<Boolean> = _locationPermissionGranted


    init {
        loadLastCity()
    }

    fun loadLastCity() {
        viewModelScope.launch {
            dataStore.lastCity.collect { city ->
                _lastCity.value = city
                if (city.isNotEmpty()) {
                    getWeather(city)
                }
            }
        }
    }

    fun getWeather(cityName: String) {
        getWeatherUseCase(cityName).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = WeatherState(weatherData = result.data)
                    saveLastCity(cityName)
                }
                is Resource.Error -> {
                    _state.value = WeatherState(
                        error = result.message ?: "An unexpected error occurred"
                    )
                }
                is Resource.Loading -> {
                    _state.value = WeatherState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }


    fun saveLastCity(cityName: String) {
        viewModelScope.launch {
            dataStore.saveLastCity(cityName)
        }
    }

    /*fun checkLocationPermission() {
        _locationPermissionGranted.value = locationManager.hasLocationPermission()
    }*/

    /*fun getWeatherByCurrentLocation() {
        viewModelScope.launch {
            _state.value = WeatherState(isLoading = true)
            val location = locationManager.getCurrentLocation()
            if (location != null) {
                getWeatherUseCase(location.latitude, location.longitude).collect { result ->
                    _state.value = when (result) {
                        is Resource.Success -> WeatherState(weatherData = result.data)
                        is Resource.Error -> WeatherState(error = result.message ?: "An unexpected error occurred")
                        is Resource.Loading -> WeatherState(isLoading = true)
                    }
                }
            } else {
                _state.value = WeatherState(error = "Unable to get current location")
            }
        }
    }*/
}

data class WeatherState(
    val isLoading: Boolean = false,
    val weatherData: WeatherData? = null,
    val error: String? = null
)