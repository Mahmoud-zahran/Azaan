package com.example.azaan.feature_prayer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azaan.core.location.LocationTracker
import com.example.azaan.feature_prayer.domain.usecase.GetTodayPrayersUseCase
import com.example.azaan.feature_prayer.presentation.state.PrayerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrayerViewModel @Inject constructor(
    private val getTodayPrayers: GetTodayPrayersUseCase,
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val _state = MutableStateFlow(PrayerUiState())
    val state = _state.asStateFlow()

    fun loadWithCurrentLocation() {
        viewModelScope.launch {
            _state.value = PrayerUiState(loading = true)
            val location = locationTracker.getCurrentLocation()
            if (location != null) {
                load(location.latitude, location.longitude)
            } else {
                _state.value = PrayerUiState(
                    error = "Could not retrieve location. Please make sure location is enabled and permissions are granted."
                )
            }
        }
    }

    private fun load(lat: Double, lng: Double) {
        viewModelScope.launch {
            getTodayPrayers(lat, lng)
                .onStart {
                    _state.value = PrayerUiState(loading = true)
                }
                .catch {
                    _state.value = PrayerUiState(error = it.message)
                }
                .collect { list ->
                    _state.value = PrayerUiState(prayers = list)
                }
        }
    }
}