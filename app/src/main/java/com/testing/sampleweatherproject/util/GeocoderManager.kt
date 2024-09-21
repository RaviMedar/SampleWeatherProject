package com.testing.sampleweatherproject.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume

class GeocoderManager(private val context: Context) {
    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())

    suspend fun getCoordinatesFromLocationName(locationName: String): Pair<Double, Double>? {
        return withContext(Dispatchers.IO) {
            try {
                val addresses: List<Address> = geocoder.getFromLocationName(locationName, 1) ?: emptyList()
                if (addresses.isNotEmpty()) {
                    Pair(addresses[0].latitude, addresses[0].longitude)
                } else {
                    null
                }
            } catch (e: IOException) {
                null
            }
        }
    }

    suspend fun getLocationNameFromCoordinates(latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1) ?: emptyList()
                if (addresses.isNotEmpty()) {
                    addresses[0].locality ?: addresses[0].adminArea ?: addresses[0].countryName
                } else {
                    null
                }
            } catch (e: IOException) {
                null
            }
        }
    }
}