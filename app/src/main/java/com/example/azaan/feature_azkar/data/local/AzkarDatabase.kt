package com.example.azaan.feature_azkar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavoriteZekr::class], version = 1, exportSchema = false)
abstract class AzkarDatabase : RoomDatabase() {
    abstract fun azkarDao(): AzkarDao
}
