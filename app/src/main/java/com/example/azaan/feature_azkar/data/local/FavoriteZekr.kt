package com.example.azaan.feature_azkar.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FavoriteZekr(
    @PrimaryKey val id: Int,
    val title: String,
    val text: String
)