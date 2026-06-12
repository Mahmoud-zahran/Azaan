package com.example.azaan.feature_azkar.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AzkarDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FavoriteZekr)

    @Delete
    suspend fun delete(item: FavoriteZekr)

    @Query("SELECT * FROM FavoriteZekr")
    fun getAll(): Flow<List<FavoriteZekr>>

    @Query("SELECT * FROM FavoriteZekr WHERE id = :id")
    suspend fun getById(id: Int): FavoriteZekr?
}
