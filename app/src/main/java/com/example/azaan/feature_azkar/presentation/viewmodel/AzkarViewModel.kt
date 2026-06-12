package com.example.azaan.feature_azkar.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azaan.feature_azkar.domain.usecase.GetAzkarUseCase
import com.example.azaan.feature_azkar.domain.usecase.ToggleFavoriteUseCase
import com.example.azaan.feature_azkar.presentation.state.AzkarUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AzkarViewModel @Inject constructor(
    private val getAzkarUseCase: GetAzkarUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AzkarUiState())
    val state: StateFlow<AzkarUiState> = _state.asStateFlow()

    init {
        _state.value = _state.value.copy(categories = listOf("morning", "evening"))
        selectCategory("morning")
    }

    fun selectCategory(category: String) {
        _state.value = _state.value.copy(
            selectedCategory = category,
            categoryDisplayName = getCategoryDisplayName(category)
        )
        loadZekrs(category)
    }

    private fun loadZekrs(category: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            getAzkarUseCase.invoke(category).collect { zekrs ->
                _state.value = _state.value.copy(
                    zekrs = zekrs,
                    loading = false
                )
            }
        }
    }

    fun toggleFavorite(zekrId: Int) {
        viewModelScope.launch {
            toggleFavoriteUseCase.invoke(zekrId)
        }
    }

    private fun getCategoryDisplayName(category: String): String = when (category) {
        "morning" -> "أذكار الصباح"
        "evening" -> "أذكار المساء"
        else -> category
    }
}
