package com.example.azaan.feature_prayer.data.local

import com.batoulapps.adhan.PrayerTimes

class PrayerLocalDataSource(
    private val calculator: PrayerCalculator
) {

    fun getPrayerTimes(lat: Double, lng: Double): PrayerTimes {
        return calculator.calculate(lat, lng)
    }
}