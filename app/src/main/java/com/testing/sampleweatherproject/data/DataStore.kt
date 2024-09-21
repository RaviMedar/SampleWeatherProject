package com.testing.sampleweatherproject.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStore(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
        private val LAST_CITY_KEY = stringPreferencesKey("last_city")
    }

    val lastCity: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_CITY_KEY] ?: ""
        }

    suspend fun saveLastCity(city: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_CITY_KEY] = city
        }
    }
}