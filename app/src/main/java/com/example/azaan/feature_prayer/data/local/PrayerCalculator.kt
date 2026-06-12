package com.example.azaan.feature_prayer.data.local

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.util.Date

class PrayerCalculator {

    fun calculate(lat: Double, lng: Double): PrayerTimes {
        val coordinates = Coordinates(lat, lng)
        val date = DateComponents.from(Date())
        val params = CalculationMethod.EGYPTIAN.getParameters()
        params.madhab = Madhab.SHAFI

        val times = PrayerTimes(
            coordinates,
            date,
            params
        )

//        times.maghrib.time = System.currentTimeMillis() + 20_000
//        times.isha.time = System.currentTimeMillis() + 40_000

        return times
    }
}