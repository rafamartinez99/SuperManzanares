package es.iessaladillo.rafamartinez.supermanzanares.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.MapboxViewModel
import kotlinx.coroutines.tasks.await

@SuppressLint("MissingPermission")
suspend fun getCurrentLocationAndFetchAddress(
    context: Context,
    mapboxViewModel: MapboxViewModel
): String? {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    if (!hasLocationPermission(context)) return null

    val location = fusedLocationClient.lastLocation.await()
    return location?.let {
        mapboxViewModel.reverseGeocode(it.latitude, it.longitude)
    }
}

@SuppressLint("MissingPermission")
suspend fun getCurrentLocation(context: Context): Pair<Double, Double>? {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    if (!hasLocationPermission(context)) return null

    val location = fusedLocationClient.lastLocation.await()
    return location?.let { Pair(it.latitude, it.longitude) }
}

private fun hasLocationPermission(context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}
