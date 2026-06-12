package com.example.azaan.core.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.azaan.core.location.LocationTracker
import com.example.azaan.feature_prayer.data.local.PrayerCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.withTimeout

@HiltWorker
class PrayerNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val prayerCalculator: PrayerCalculator,
    private val locationTracker: LocationTracker
) : CoroutineWorker(context, params) {

    companion object {
        private const val FALLBACK_LAT = 30.0444
        private const val FALLBACK_LNG = 31.2357
    }

    override suspend fun doWork(): Result {
        return try {
            val location = try {
                withTimeout(10_000) { locationTracker.getCurrentLocation() }
            } catch (_: Exception) {
                null
            }
            val lat = location?.latitude ?: FALLBACK_LAT
            val lng = location?.longitude ?: FALLBACK_LNG

            val prayerTimes = prayerCalculator.calculate(lat, lng)
            AlarmScheduler.scheduleAll(applicationContext, prayerTimes)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
