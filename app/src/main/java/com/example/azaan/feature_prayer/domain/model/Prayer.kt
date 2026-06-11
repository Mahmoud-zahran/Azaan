package com.example.azaan.feature_prayer.domain.model

data class Prayer(
    val name: String,
    val time: String,
    val isNext: Boolean = false
)