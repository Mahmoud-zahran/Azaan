package com.example.azaan.feature_azkar.domain.repository

import com.example.azaan.feature_azkar.domain.model.Zekr
import kotlinx.coroutines.flow.Flow

interface AzkarRepository {
    fun getZekrsByCategory(category: String): Flow<List<Zekr>>
    fun getAllCategories(): List<String>
    fun getCategoryDisplayName(category: String): String
    suspend fun toggleFavorite(zekrId: Int)
    fun getFavoriteIds(): Flow<Set<Int>>
}
