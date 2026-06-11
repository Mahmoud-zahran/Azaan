package com.example.azaan.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AthanService.ACTION_STOP_ATHAN) {
            val stopIntent = Intent(context, AthanService::class.java).apply {
                action = AthanService.ACTION_STOP_ATHAN
            }
            context.startForegroundService(stopIntent)
            return
        }

        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "Prayer"
        val serviceIntent = Intent(context, AthanService::class.java).apply {
            putExtra(AthanService.EXTRA_PRAYER_NAME, prayerName)
        }
        context.startForegroundService(serviceIntent)
    }
}
