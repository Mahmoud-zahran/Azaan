package com.example.azaan.core.utils

data class HijriDate(val year: Int, val month: Int, val day: Int) {
    val monthName: String
        get() = HIJRI_MONTHS[month - 1]

    override fun toString(): String {
        val suffixes = arrayOf("", "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
            "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
            "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah")
        return "$day ${suffixes[month]} $year AH"
    }

    companion object {
        val HIJRI_MONTHS = arrayOf(
            "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
            "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
            "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
        )
    }
}

object HijriDateConverter {
    private const val HIJRI_EPOCH = 1948440
    private const val CYCLE_DAYS = 10631
    private val LEAP_YEARS = setOf(2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29)

    private val GREGORIAN_MONTHS = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    fun toHijri(year: Int, month: Int, day: Int): HijriDate {
        val jdn = gregorianToJdn(year, month, day)
        return jdnToHijri(jdn)
    }

    fun formatGregorian(year: Int, month: Int, day: Int): String {
        return "${GREGORIAN_MONTHS[month - 1]} $day, $year"
    }

    private fun gregorianToJdn(y: Int, m: Int, d: Int): Int {
        val a = (14 - m) / 12
        val year = y + 4800 - a
        val month = m + 12 * a - 3
        return d + (153 * month + 2) / 5 + 365 * year + year / 4 - year / 100 + year / 400 - 32045
    }

    private fun jdnToHijri(jdn: Int): HijriDate {
        val days = jdn - HIJRI_EPOCH
        if (days < 0) return HijriDate(1, 1, 1)

        val cycles = days / CYCLE_DAYS
        var dayInCycle = days % CYCLE_DAYS

        var year = cycles * 30 + 1
        var yic = 1

        while (yic <= 30) {
            val daysInYear = if (yic in LEAP_YEARS) 355 else 354
            if (dayInCycle < daysInYear) break
            dayInCycle -= daysInYear
            year++
            yic++
        }

        val isLeap = yic in LEAP_YEARS
        val monthDays = intArrayOf(30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, if (isLeap) 30 else 29)

        var month = 1
        for (md in monthDays) {
            if (dayInCycle < md) break
            dayInCycle -= md
            month++
        }

        return HijriDate(year, month, dayInCycle + 1)
    }
}
