package com.example.azaan

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.azaan.core.location.LocationTracker
import com.example.azaan.core.notification.AlarmScheduler
import com.example.azaan.core.notification.KeepAliveService
import com.example.azaan.core.notification.PrayerNotificationWorker
import com.example.azaan.feature_prayer.data.local.PrayerCalculator
import com.example.azaan.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prayerCalculator: PrayerCalculator
    @Inject lateinit var locationTracker: LocationTracker

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()
        requestExactAlarmPermissionIfNeeded()
        requestIgnoreBatteryOptimizations()
        startKeepAliveService()
        setupPrayerNotifications()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:$packageName")
                ).also { startActivity(it) }
            }
        }
    }

    private fun requestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:$packageName")
                ).also { startActivity(it) }
            }
        }
    }

    private fun setupPrayerNotifications() {
        val periodicRequest = PeriodicWorkRequestBuilder<PrayerNotificationWorker>(
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "PrayerNotificationWork",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )

        val immediateRequest = OneTimeWorkRequestBuilder<PrayerNotificationWorker>().build()
        WorkManager.getInstance(applicationContext).enqueue(immediateRequest)

        lifecycleScope.launch {
            try {
                withTimeout(10_000) {
                    val location = locationTracker.getCurrentLocation()
                    val lat = location?.latitude ?: 30.0444
                    val lng = location?.longitude ?: 31.2357

                    val intent = Intent(this@MainActivity, KeepAliveService::class.java).apply {
                        action = KeepAliveService.ACTION_UPDATE_LOCATION
                        putExtra(KeepAliveService.EXTRA_LAT, lat)
                        putExtra(KeepAliveService.EXTRA_LNG, lng)
                    }
                    startService(intent)

                    val prayerTimes = prayerCalculator.calculate(lat, lng)
                    AlarmScheduler.scheduleAll(this@MainActivity, prayerTimes)
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun startKeepAliveService() {
        val intent = Intent(this, KeepAliveService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
