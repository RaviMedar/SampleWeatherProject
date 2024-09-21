package com.testing.sampleweatherproject.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.testing.sampleweatherproject.data.DataStore
import com.testing.sampleweatherproject.data.model.WeatherData
import com.testing.sampleweatherproject.domain.GetWeatherUseCase
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
    private val dataStore: DataStore
) : ViewModel() {

    private val _state = MutableStateFlow(WeatherState())
    val state: StateFlow<WeatherState> = _state

    private val _lastCity = MutableStateFlow("")
    val lastCity: StateFlow<String> = _lastCity

    init {
        loadLastCity()
    }

    private fun loadLastCity() {
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

    private fun saveLastCity(cityName: String) {
        viewModelScope.launch {
            dataStore.saveLastCity(cityName)
        }
    }
}

data class WeatherState(
    val isLoading: Boolean = false,
    val weatherData: WeatherData? = null,
    val error: String? = null
)