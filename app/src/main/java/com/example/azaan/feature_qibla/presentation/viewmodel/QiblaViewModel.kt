package com.example.azaan.feature_qibla.presentation.viewmodel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azaan.core.location.LocationTracker
import com.example.azaan.feature_qibla.data.QiblaCalculator
import com.example.azaan.feature_qibla.presentation.state.QiblaUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QiblaViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationTracker: LocationTracker,
    private val qiblaCalculator: QiblaCalculator
) : ViewModel(), SensorEventListener {

    private val _state = MutableStateFlow(QiblaUiState())
    val state: StateFlow<QiblaUiState> = _state.asStateFlow()

    private var sensorManager: SensorManager? = null
    private var rotationSensor: Sensor? = null
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var currentLat = 30.0444
    private var currentLng = 31.2357

    init {
        loadLocationAndSetup()
    }

    private fun loadLocationAndSetup() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val location = locationTracker.getCurrentLocation()
            if (location != null) {
                currentLat = location.latitude
                currentLng = location.longitude
            }
            setupSensor()
        }
    }

    private fun setupSensor() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationSensor != null) {
            sensorManager?.registerListener(
                this,
                rotationSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        } else {
            _state.value = _state.value.copy(
                error = "Compass sensor not available",
                loading = false
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            val normalizedAzimuth = (azimuth + 360) % 360

            val direction = qiblaCalculator.calculateDirection(
                currentLat, currentLng, normalizedAzimuth
            )
            _state.value = _state.value.copy(
                direction = direction,
                loading = false
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _state.value = _state.value.copy(compassAccuracy = accuracy)
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager?.unregisterListener(this)
    }
}
