package com.testing.sampleweatherproject.domain

import com.testing.sampleweatherproject.data.WeatherRepository
import com.testing.sampleweatherproject.data.model.WeatherData
import com.testing.sampleweatherproject.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    operator fun invoke(cityName: String): Flow<Resource<WeatherData>> {
        return repository.getWeather(cityName)
    }
}