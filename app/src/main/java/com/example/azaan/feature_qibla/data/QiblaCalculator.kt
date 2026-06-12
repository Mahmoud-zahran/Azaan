package com.example.azaan.feature_qibla.data

import com.example.azaan.feature_qibla.domain.QiblaDirection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QiblaCalculator @Inject constructor() {

    companion object {
        private const val MECCA_LAT = 21.4225
        private const val MECCA_LNG = 39.8262
    }

    fun calculateQibla(lat: Double, lng: Double): Float {
        val phiK = Math.toRadians(MECCA_LAT)
        val phi = Math.toRadians(lat)
        val deltaLambda = Math.toRadians(MECCA_LNG - lng)

        val y = Math.sin(deltaLambda)
        val x = Math.cos(phi) * Math.tan(phiK) - Math.sin(phi) * Math.cos(deltaLambda)

        var qibla = Math.toDegrees(Math.atan2(y, x))
        qibla = (qibla + 360) % 360
        return qibla.toFloat()
    }

    fun calculateDirection(
        lat: Double,
        lng: Double,
        deviceAzimuth: Float
    ): QiblaDirection {
        val qiblaAngle = calculateQibla(lat, lng)
        val bearing = (qiblaAngle - deviceAzimuth + 360) % 360
        return QiblaDirection(
            qiblaAngle = qiblaAngle,
            deviceAzimuth = deviceAzimuth,
            bearingToQibla = bearing
        )
    }
}
