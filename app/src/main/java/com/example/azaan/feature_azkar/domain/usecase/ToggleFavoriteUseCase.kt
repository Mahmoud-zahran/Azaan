package com.example.azaan.feature_azkar.domain.usecase

import com.example.azaan.feature_azkar.domain.repository.AzkarRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: AzkarRepository
) {
    suspend operator fun invoke(zekrId: Int) =
        repository.toggleFavorite(zekrId)
}
