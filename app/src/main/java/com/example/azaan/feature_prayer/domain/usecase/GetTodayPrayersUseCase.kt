package com.example.azaan.feature_prayer.domain.usecase

import com.example.azaan.feature_prayer.domain.model.Prayer
import com.example.azaan.feature_prayer.domain.repository.PrayerRepository
import kotlinx.coroutines.flow.Flow

class GetTodayPrayersUseCase(
    private val repository: PrayerRepository
) {
    operator fun invoke(
        lat: Double,
        lng: Double
    ): Flow<List<Prayer>> {
        return repository.getTodayPrayers(lat, lng)
    }
}