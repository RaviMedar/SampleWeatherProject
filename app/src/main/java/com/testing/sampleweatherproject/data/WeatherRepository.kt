package com.testing.sampleweatherproject.data

import android.content.Context
import android.location.Geocoder
import com.testing.sampleweatherproject.data.model.WeatherData
import com.testing.sampleweatherproject.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val api: WeatherApi,
    private val context: Context
) {
    fun getWeather(cityName: String): Flow<Resource<WeatherData>> = flow {
        emit(Resource.Loading())

        try {
            val geocoder = Geocoder(context)
            val addresses = geocoder.getFromLocationName(cityName, 1)

            if (addresses.isNullOrEmpty()) {
                emit(Resource.Error("Location not found"))
                return@flow
            }

            val lat = addresses[0].latitude
            val lon = addresses[0].longitude

            val response = api.getWeather(lat, lon, API_KEY)
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    companion object {
        private const val API_KEY = "f54765da21650aa469e2e1903fd721d2"
    }
}