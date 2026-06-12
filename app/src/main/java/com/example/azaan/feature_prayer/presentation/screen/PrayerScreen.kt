package com.example.azaan.feature_prayer.presentation.screen

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.azaan.core.notification.AthanPlayer
import com.example.azaan.core.notification.PrayerNotificationReceiver
import com.example.azaan.core.utils.HijriDateConverter
import com.example.azaan.feature_prayer.domain.model.Prayer
import com.example.azaan.feature_prayer.presentation.viewmodel.PrayerViewModel
import java.util.Calendar
import java.util.TimeZone

private val DAY_NAMES = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
private val MONTH_NAMES = arrayOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerScreen(
    viewModel: PrayerViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val context = LocalContext.current
    val athanPlayer = remember { AthanPlayer(context) }

    val todayCal = remember {
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = System.currentTimeMillis()
        }
    }
    var selectedYear by remember { mutableIntStateOf(todayCal.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(todayCal.get(Calendar.MONTH) + 1) }
    var selectedDay by remember { mutableIntStateOf(todayCal.get(Calendar.DAY_OF_MONTH)) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.values.any { it }
        if (isGranted) {
            viewModel.loadWithCurrentLocation()
        }
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    val daysList = remember(selectedYear, selectedMonth, selectedDay) {
        generateDaysAround(selectedYear, selectedMonth, selectedDay)
    }

    val hijriDate = remember(selectedYear, selectedMonth, selectedDay) {
        HijriDateConverter.toHijri(selectedYear, selectedMonth, selectedDay)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Azaan Home") },
                actions = {
                    IconButton(onClick = { athanPlayer.playAthan() }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Test Sound")
                    }
                    IconButton(onClick = { scheduleTestAlarm(context) }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Test Alarm")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            val error = state.error
            when {
                state.loading -> {
                    CircularProgressIndicator()
                }
                error != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(onClick = { viewModel.loadWithCurrentLocation() }) {
                            Text("Retry")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            CalendarStrip(
                                days = daysList,
                                selectedYear = selectedYear,
                                selectedMonth = selectedMonth,
                                selectedDay = selectedDay,
                                onDaySelected = { y, m, d ->
                                    selectedYear = y
                                    selectedMonth = m
                                    selectedDay = d
                                }
                            )
                            HijriDateCard(
                                gregorian = "${MONTH_NAMES[selectedMonth - 1]} $selectedDay, $selectedYear",
                                hijri = hijriDate.toString(),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Prayer Times Today",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(state.prayers) { prayer ->
                            PrayerItem(prayer)
                        }
                    }
                }
            }
        }
    }
}

data class DayItem(val year: Int, val month: Int, val day: Int, val dayOfWeek: Int, val isToday: Boolean)

@Composable
private fun CalendarStrip(
    days: List<DayItem>,
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    onDaySelected: (Int, Int, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "${MONTH_NAMES[selectedMonth - 1]} $selectedYear",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                textAlign = TextAlign.Center
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                userScrollEnabled = true
            ) {
                items(days) { dayItem ->
                    DayCell(
                        dayItem = dayItem,
                        isSelected = dayItem.year == selectedYear &&
                                dayItem.month == selectedMonth &&
                                dayItem.day == selectedDay,
                        onClick = { onDaySelected(dayItem.year, dayItem.month, dayItem.day) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    dayItem: DayItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val weight = if (isSelected) FontWeight.Bold else FontWeight.Normal

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(44.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = DAY_NAMES[dayItem.dayOfWeek].take(2),
            fontSize = 10.sp,
            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayItem.day.toString(),
                fontSize = 14.sp,
                fontWeight = weight,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HijriDateCard(
    gregorian: String,
    hijri: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = gregorian,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = hijri,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.End
            )
        }
    }
}

private fun generateDaysAround(year: Int, month: Int, day: Int): List<DayItem> {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, day)
    }

    val todayCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = System.currentTimeMillis()
    }
    val todayYear = todayCal.get(Calendar.YEAR)
    val todayMonth = todayCal.get(Calendar.MONTH) + 1
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

    val result = mutableListOf<DayItem>()

    cal.add(Calendar.DAY_OF_MONTH, -7)

    repeat(15) {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val dow = cal.get(Calendar.DAY_OF_WEEK) - 1
        val isToday = y == todayYear && m == todayMonth && d == todayDay
        result.add(DayItem(y, m, d, dow, isToday))
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }

    return result
}

private fun scheduleTestAlarm(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            context.startActivity(intent)
            return
        }
    }

    val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
        putExtra("PRAYER_NAME", "Test Prayer")
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        9999,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val triggerAt = System.currentTimeMillis() + 5000

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        triggerAt,
        pendingIntent
    )
}

@Composable
fun PrayerItem(prayer: Prayer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (prayer.isNext)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = prayer.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = if (prayer.isNext) FontWeight.Bold else FontWeight.Normal
                )
            )
            Text(
                text = prayer.time,
                style = MaterialTheme.typography.bodyLarge,
                color = if (prayer.isNext)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
