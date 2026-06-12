package com.example.azaan.navigation

sealed class Screen(val route: String) {
    data object Prayer : Screen("prayer")
    data object Azkar : Screen("azkar")
    data object Qibla : Screen("qibla")
}
