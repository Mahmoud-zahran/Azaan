package com.example.azaan.feature_qibla.presentation.state

import com.example.azaan.feature_qibla.domain.QiblaDirection

data class QiblaUiState(
    val loading: Boolean = true,
    val direction: QiblaDirection? = null,
    val error: String? = null,
    val compassAccuracy: Int = 0
)
