package com.example.azaan.feature_prayer.data.local

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.util.Date
import java.util.Calendar

class PrayerCalculator {

    fun calculate(lat: Double, lng: Double): PrayerTimes {
        val coordinates = Coordinates(lat, lng)
        val date = DateComponents.from(Date())
        val params = CalculationMethod.EGYPTIAN.getParameters()
        params.madhab = Madhab.SHAFI

        return PrayerTimes(
            coordinates,
            date,
            params
        )
    }
}