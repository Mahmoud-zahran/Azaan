package com.example.azaan.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.azaan.feature_prayer.data.local.PrayerCalculator

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val FALLBACK_LAT = 30.0444
        private const val FALLBACK_LNG = 31.2357
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val calculator = PrayerCalculator()
        val prayerTimes = calculator.calculate(FALLBACK_LAT, FALLBACK_LNG)
        AlarmScheduler.scheduleAll(context, prayerTimes)
    }
}
