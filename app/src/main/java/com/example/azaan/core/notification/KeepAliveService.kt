package com.example.azaan.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.example.azaan.MainActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KeepAliveService : Service() {

    companion object {
        const val CHANNEL_ID = "keep_alive"
        const val NOTIFICATION_ID = 1002
        const val ACTION_UPDATE_LOCATION = "com.example.azaan.UPDATE_LOCATION"
        const val EXTRA_LAT = "lat"
        const val EXTRA_LNG = "lng"
        private const val FALLBACK_LAT = 30.0444
        private const val FALLBACK_LNG = 31.2357
    }

    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val handler = Handler(Looper.getMainLooper())
    private var updateTask: Runnable? = null
    private var currentLat = FALLBACK_LAT
    private var currentLng = FALLBACK_LNG

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_UPDATE_LOCATION) {
            intent.getDoubleExtra(EXTRA_LAT, FALLBACK_LAT).let { currentLat = it }
            intent.getDoubleExtra(EXTRA_LNG, FALLBACK_LNG).let { currentLng = it }
            val notification = buildNotification()
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
            return START_STICKY
        }

        try {
            startForeground(NOTIFICATION_ID, buildNotification())
            startUpdating()
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        updateTask?.let { handler.removeCallbacks(it) }
        super.onDestroy()
    }

    private fun buildNotification(): android.app.Notification {
        val nextInfo = getNextPrayerInfo()
        val text = if (nextInfo != null) {
            "${nextInfo.first} at ${timeFormatter.format(nextInfo.second)}"
        } else {
            "Prayer alerts active"
        }

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = android.app.PendingIntent.getActivity(
            this, 0, openIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Azaan: $text")
            .setContentText("Tap to view full prayer times")
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    private fun getNextPrayerInfo(): Pair<String, Date>? {
        val coordinates = Coordinates(currentLat, currentLng)
        val date = DateComponents.from(Date())
        val params = CalculationMethod.EGYPTIAN.getParameters()
        params.madhab = Madhab.SHAFI

        val times = PrayerTimes(coordinates, date, params)
        val now = System.currentTimeMillis()

        val entries = listOf(
            "Fajr" to times.fajr,
            "Dhuhr" to times.dhuhr,
            "Asr" to times.asr,
            "Maghrib" to times.maghrib,
            "Isha" to times.isha
        )

        return entries.firstOrNull { it.second.time > now }
    }

    private fun startUpdating() {
        updateTask?.let { handler.removeCallbacks(it) }
        val task = Runnable {
            val notification = buildNotification()
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
            updateTask?.let { handler.postDelayed(it, 60_000) }
        }
        updateTask = task
        handler.postDelayed(task, 60_000)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Keep Alive",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Keeps the app running for prayer alerts"
                setSound(null, null)
                enableVibration(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
