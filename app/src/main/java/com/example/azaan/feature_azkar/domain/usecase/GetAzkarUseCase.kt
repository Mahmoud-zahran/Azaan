package com.example.azaan.feature_azkar.domain.usecase

import com.example.azaan.feature_azkar.domain.model.Zekr
import com.example.azaan.feature_azkar.domain.repository.AzkarRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAzkarUseCase @Inject constructor(
    private val repository: AzkarRepository
) {
    operator fun invoke(category: String): Flow<List<Zekr>> =
        repository.getZekrsByCategory(category)
}
