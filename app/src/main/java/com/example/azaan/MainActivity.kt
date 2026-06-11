package com.example.azaan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.azaan.core.notification.PrayerNotificationWorker
import com.example.azaan.feature_prayer.presentation.screen.PrayerScreen
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupPrayerNotifications()
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PrayerScreen()
                }
            }
        }
    }

    private fun setupPrayerNotifications() {
        val workRequest = PeriodicWorkRequestBuilder<PrayerNotificationWorker>(
            24, TimeUnit.HOURS
        ).build()
        
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "PrayerNotificationWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}