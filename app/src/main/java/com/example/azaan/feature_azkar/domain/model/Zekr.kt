package com.example.azaan.feature_azkar.domain.model

data class Zekr(
    val id: Int,
    val title: String,
    val text: String,
    val repeat: Int,
    val reference: String,
    val category: String,
    val isFavorite: Boolean = false
)
