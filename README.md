# Azaan
Android Athan + Azkar app,

✓ Prayer Times

✓ Athan Notifications

✓ Morning Azkar

✓ Evening Azkar

✓ Sleep Azkar

✓ Prayer Azkar

✓ Qibla Compass

✓ Hijri Date

✓ Digital Tasbeeh

✓ Settings


I would avoid depending completely on an online API. A better architecture is:

✅ Prayer times calculated locally (works offline)

✅ Free and open-source libraries

✅ Trusted Islamic data sources

✅ Optional API only for backup or Hijri calendar updates

1. Prayer Times (Best Choice)
Use: Adhan Java

GitHub:
Adhan Java

Advantages:

Free and open source
Used by many Muslim apps
Accurate calculations
Supports:
Muslim World League
Umm Al-Qura
Egyptian General Authority of Survey
ISNA
Karachi
Kuwait
Qatar
Dubai
and many others

Example:

val coordinates = Coordinates(30.0444, 31.2357)

val params = CalculationMethod.EGYPTIAN.getParameters()
params.madhab = Madhab.SHAFI

val prayerTimes = PrayerTimes(
    coordinates,
    Date(),
    params
)

val fajr = prayerTimes.fajr
val dhuhr = prayerTimes.dhuhr
2. Hijri Calendar
Option A (Recommended)

Use AlAdhan API:

AlAdhan Islamic Calendar API

Features:

Gregorian ↔ Hijri conversion
Islamic holidays
Ramadan support
3. Prayer Times API (Optional)

If you need cloud synchronization:

AlAdhan Prayer Times API

Features:

Free
No API key required for normal usage
Prayer times
Qibla
Hijri calendar
Multiple calculation methods
4. Azkar Data

For Azkar, I recommend storing them locally in JSON.

Example:

{
  "category": "Morning Azkar",
  "title": "Ayat Al-Kursi",
  "arabic": "...",
  "translation": "...",
  "repeat": 1
}

Create:

assets/
   azkar/
      morning.json
      evening.json
      sleep.json
      prayer.json

Parse using:

Gson
or
kotlinx.serialization

This makes the app fully offline.

5. Quran (If you plan to add it)

Good APIs:

Al Quran Cloud API
UmmahAPI
6. Qibla Direction

Calculate using:

Device GPS
Android Compass Sensor
Great-circle bearing formula

No external API is required.

7. Android Architecture

Since you already work with modern Android:

Jetpack Compose
MVVM
Clean Architecture
Hilt
Room
WorkManager
DataStore
Retrofit
Coroutines + Flow

Project structure:

app

├── data

│   ├── local

│   ├── remote

│   └── repository

├── domain

├── presentation

├── prayer

├── azkar

├── quran

├── qibla

└── notifications


8. Main Features
* Prayer Times
* Athan Notifications
* Qibla Compass
* Morning Azkar
* Evening Azkar
* Sleep Azkar
* Prayer Azkar
* Digital Tasbeeh Counter
* Hijri Calendar
* Ramadan Mode
* Prayer Time Widget
* Offline Support
* Arabic & English
  
========================

My recommended stack

Feature	Library/API

Prayer Time Calculation	Adhan Java

Hijri Calendar	AlAdhan API

Azkar	Local JSON assets

Qibla	Android Sensors + GPS

Notifications	WorkManager

Local Storage	Room + DataStore

Networking	Retrofit

Dependency Injection

Hilt
