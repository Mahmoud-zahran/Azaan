package com.example.azaan.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.azaan.core.location.LocationTracker
import com.example.azaan.feature_prayer.data.local.PrayerCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*

@HiltWorker
class PrayerNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val prayerCalculator: PrayerCalculator,
    private val locationTracker: LocationTracker
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val location = locationTracker.getCurrentLocation() ?: return Result.retry()
        
        val prayerTimes = prayerCalculator.calculate(location.latitude, location.longitude)
        
        scheduleAlarm("Fajr", prayerTimes.fajr.time)
        scheduleAlarm("Dhuhr", prayerTimes.dhuhr.time)
        scheduleAlarm("Asr", prayerTimes.asr.time)
        scheduleAlarm("Maghrib", prayerTimes.maghrib.time)
        scheduleAlarm("Isha", prayerTimes.isha.time)

        return Result.success()
    }

    private fun scheduleAlarm(prayerName: String, triggerAtMillis: Long) {
        if (triggerAtMillis < System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
            putExtra("PRAYER_NAME", prayerName)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            prayerName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }
}