package com.example.azaan.feature_azkar.presentation.state

import com.example.azaan.feature_azkar.domain.model.Zekr

data class AzkarUiState(
    val loading: Boolean = true,
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "morning",
    val zekrs: List<Zekr> = emptyList(),
    val categoryDisplayName: String = "",
    val error: String? = null
)
