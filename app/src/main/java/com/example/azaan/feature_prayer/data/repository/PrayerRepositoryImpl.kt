package com.example.azaan.feature_prayer.data.repository

import com.example.azaan.feature_prayer.data.local.PrayerLocalDataSource
import com.example.azaan.feature_prayer.domain.model.Prayer
import com.example.azaan.feature_prayer.domain.repository.PrayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PrayerRepositoryImpl(
    private val localDataSource: PrayerLocalDataSource
) : PrayerRepository {

    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    override fun getTodayPrayers(
        lat: Double,
        lng: Double
    ): Flow<List<Prayer>> = flow {

        val times = localDataSource.getPrayerTimes(lat, lng)
        val next = times.nextPrayer()

        val list = listOf(
            Prayer("Fajr", timeFormatter.format(times.fajr), next == com.batoulapps.adhan.Prayer.FAJR),
            Prayer("Sunrise", timeFormatter.format(times.sunrise), next == com.batoulapps.adhan.Prayer.SUNRISE),
            Prayer("Dhuhr", timeFormatter.format(times.dhuhr), next == com.batoulapps.adhan.Prayer.DHUHR),
            Prayer("Asr", timeFormatter.format(times.asr), next == com.batoulapps.adhan.Prayer.ASR),
            Prayer("Maghrib", timeFormatter.format(
                times.maghrib
//                Calendar.getInstance().apply { add(Calendar.MINUTE, 1   ) }.time
            ), next == com.batoulapps.adhan.Prayer.MAGHRIB),
            Prayer("Isha", timeFormatter.format(times.isha), next == com.batoulapps.adhan.Prayer.ISHA)
        )

        emit(list)
    }
}