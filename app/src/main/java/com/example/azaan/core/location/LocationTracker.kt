package com.example.azaan.core.location

import android.location.Location

interface LocationTracker {
    suspend fun getCurrentLocation(): Location?
}