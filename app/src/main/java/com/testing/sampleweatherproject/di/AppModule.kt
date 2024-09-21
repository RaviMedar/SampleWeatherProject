package com.testing.sampleweatherproject.di

import android.content.Context
import com.testing.sampleweatherproject.data.DataStore
import com.testing.sampleweatherproject.data.WeatherApi
import com.testing.sampleweatherproject.data.WeatherRepository
import com.testing.sampleweatherproject.util.GeocoderManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWeatherApi(): WeatherApi {
        return Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(api: WeatherApi, @ApplicationContext context: Context, geocoderManager: GeocoderManager): WeatherRepository {
        return WeatherRepository(api, context, geocoderManager )
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore {
        return DataStore(context)
    }

    @Provides
    @Singleton
    fun provideGeocoderManager(@ApplicationContext context: Context): GeocoderManager {
        return GeocoderManager(context)
    }
}