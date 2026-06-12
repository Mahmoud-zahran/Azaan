package com.example.azaan.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.batoulapps.adhan.PrayerTimes

object AlarmScheduler {

    fun scheduleAll(context: Context, prayerTimes: PrayerTimes) {
        schedulePrayer(context, "Fajr", prayerTimes.fajr.time)
        schedulePrayer(context, "Dhuhr", prayerTimes.dhuhr.time)
        schedulePrayer(context, "Asr", prayerTimes.asr.time)
        schedulePrayer(context, "Maghrib", prayerTimes.maghrib.time)
        schedulePrayer(context, "Isha", prayerTimes.isha.time)
    }

    fun schedulePrayer(context: Context, prayerName: String, triggerAtMillis: Long) {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }
}
