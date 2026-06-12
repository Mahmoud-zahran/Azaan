package com.example.azaan.core.di

import android.app.Application
import androidx.room.Room
import com.example.azaan.core.location.DefaultLocationTracker
import com.example.azaan.core.location.LocationTracker
import com.example.azaan.feature_azkar.data.local.AzkarDao
import com.example.azaan.feature_azkar.data.local.AzkarDatabase
import com.example.azaan.feature_azkar.data.local.AzkarJsonDataSource
import com.example.azaan.feature_azkar.data.repository.AzkarRepositoryImpl
import com.example.azaan.feature_azkar.domain.repository.AzkarRepository
import com.example.azaan.feature_azkar.domain.usecase.GetAzkarUseCase
import com.example.azaan.feature_azkar.domain.usecase.ToggleFavoriteUseCase
import com.example.azaan.feature_prayer.data.local.PrayerCalculator
import com.example.azaan.feature_prayer.data.local.PrayerLocalDataSource
import com.example.azaan.feature_prayer.data.repository.PrayerRepositoryImpl
import com.example.azaan.feature_prayer.domain.repository.PrayerRepository
import com.example.azaan.feature_prayer.domain.usecase.GetTodayPrayersUseCase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun providePrayerCalculator(): PrayerCalculator {
        return PrayerCalculator()
    }

    @Provides
    @Singleton
    fun providePrayerLocalDataSource(calculator: PrayerCalculator): PrayerLocalDataSource {
        return PrayerLocalDataSource(calculator)
    }

    @Provides
    @Singleton
    fun providePrayerRepository(localDataSource: PrayerLocalDataSource): PrayerRepository {
        return PrayerRepositoryImpl(localDataSource)
    }

    @Provides
    @Singleton
    fun provideGetTodayPrayersUseCase(repository: PrayerRepository): GetTodayPrayersUseCase {
        return GetTodayPrayersUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(app: Application): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(app)
    }

    @Provides
    @Singleton
    fun provideLocationTracker(
        fusedLocationProviderClient: FusedLocationProviderClient,
        app: Application
    ): LocationTracker {
        return DefaultLocationTracker(fusedLocationProviderClient, app)
    }

    @Provides
    @Singleton
    fun provideAzkarDatabase(app: Application): AzkarDatabase {
        return Room.databaseBuilder(
            app,
            AzkarDatabase::class.java,
            "azkar_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideAzkarDao(database: AzkarDatabase): AzkarDao {
        return database.azkarDao()
    }

    @Provides
    @Singleton
    fun provideAzkarRepository(
        jsonDataSource: AzkarJsonDataSource,
        dao: AzkarDao
    ): AzkarRepository {
        return AzkarRepositoryImpl(jsonDataSource, dao)
    }

    @Provides
    @Singleton
    fun provideGetAzkarUseCase(repository: AzkarRepository): GetAzkarUseCase {
        return GetAzkarUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideToggleFavoriteUseCase(repository: AzkarRepository): ToggleFavoriteUseCase {
        return ToggleFavoriteUseCase(repository)
    }


}
