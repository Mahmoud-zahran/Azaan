package com.example.azaan.core.location

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class DefaultLocationTracker @Inject constructor(
    private val locationClient: FusedLocationProviderClient,
    private val application: Application
) : LocationTracker {

    override suspend fun getCurrentLocation(): Location? {
        val hasFine = ContextCompat.checkSelfPermission(
            application, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            application, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) return null

        return suspendCancellableCoroutine { continuation ->
            locationClient.lastLocation.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(task.result)
                } else {
                    continuation.resume(null)
                }
            }
        }
    }
}
