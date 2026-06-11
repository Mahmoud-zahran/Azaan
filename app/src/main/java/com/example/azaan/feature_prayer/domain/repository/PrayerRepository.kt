package com.example.azaan.feature_prayer.domain.repository

import com.example.azaan.feature_prayer.domain.model.Prayer
import kotlinx.coroutines.flow.Flow

interface PrayerRepository {

    fun getTodayPrayers(
        lat: Double,
        lng: Double
    ): Flow<List<Prayer>>

}