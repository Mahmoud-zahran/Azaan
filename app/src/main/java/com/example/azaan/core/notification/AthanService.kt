package com.example.azaan.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.azaan.MainActivity
import com.example.azaan.R

class AthanService : Service() {

    companion object {
        const val CHANNEL_ID = "athan_playback"
        const val ACTION_STOP_ATHAN = "com.example.azaan.ACTION_STOP_ATHAN"
        const val EXTRA_PRAYER_NAME = "PRAYER_NAME"
        const val NOTIFICATION_ID = 1001
    }

    private val athanPlayer by lazy { AthanPlayer(this) }
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_ATHAN -> {
                stopAthanAndSelf()
                return START_NOT_STICKY
            }
            else -> {
                val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "Prayer"
                acquireWakeLock()
                try {
                    startForeground(NOTIFICATION_ID, buildNotification(prayerName))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                athanPlayer.playAthan {
                    releaseWakeLock()
                    try {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    } catch (_: Exception) {}
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(prayerName: String): android.app.Notification {
        val stopIntent = Intent(this, AthanService::class.java).apply {
            action = ACTION_STOP_ATHAN
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_PRAYER_NAME, prayerName)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 1, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(prayerName)
            .setContentText("Time for $prayerName prayer")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Athan Playback",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Ongoing notification while athan is playing"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AthanService:AudioLock"
        ).apply {
            setReferenceCounted(false)
            acquire(5 * 60 * 1000L)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    private fun stopAthanAndSelf() {
        athanPlayer.stopAthan()
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}
