package com.example.azaan.feature_azkar.data.repository

import com.example.azaan.feature_azkar.data.local.AzkarDao
import com.example.azaan.feature_azkar.data.local.AzkarJsonDataSource
import com.example.azaan.feature_azkar.data.local.FavoriteZekr
import com.example.azaan.feature_azkar.domain.model.Zekr
import com.example.azaan.feature_azkar.domain.repository.AzkarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AzkarRepositoryImpl @Inject constructor(
    private val jsonDataSource: AzkarJsonDataSource,
    private val dao: AzkarDao
) : AzkarRepository {

    override fun getZekrsByCategory(category: String): Flow<List<Zekr>> {
        val zekrs = jsonDataSource.getZekrsByCategory(category)
        return dao.getAll().map { favorites ->
            val favoriteIds = favorites.map { it.id }.toSet()
            zekrs.map { zekr ->
                zekr.copy(isFavorite = zekr.id in favoriteIds)
            }
        }
    }

    override fun getAllCategories(): List<String> =
        jsonDataSource.getAllCategories()

    override fun getCategoryDisplayName(category: String): String =
        jsonDataSource.getCategoryDisplayName(category)

    override suspend fun toggleFavorite(zekrId: Int) {
        val existing = dao.getById(zekrId)
        if (existing != null) {
            dao.delete(existing)
        } else {
            val zekr = jsonDataSource.getZekrsByCategory("morning")
                .plus(jsonDataSource.getZekrsByCategory("evening"))
                .find { it.id == zekrId }
            zekr?.let {
                dao.insert(FavoriteZekr(id = it.id, title = it.title, text = it.text))
            }
        }
    }

    override fun getFavoriteIds(): Flow<Set<Int>> =
        dao.getAll().map { favorites -> favorites.map { it.id }.toSet() }
}
