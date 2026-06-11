package com.example.azaan.feature_prayer.presentation.state

import com.example.azaan.feature_prayer.domain.model.Prayer

data class PrayerUiState(
    val loading: Boolean = false,
    val prayers: List<Prayer> = emptyList(),
    val error: String? = null
)