package com.ernesto.playout.di

import android.content.Context
import androidx.room.Room
import com.ernesto.playout.data.db.AppDatabase
import com.ernesto.playout.data.db.CustomFacilityDao
import com.ernesto.playout.data.db.FacilityDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        AppDatabase.appContext = context
        return Room.databaseBuilder(context, AppDatabase::class.java, "playout21.db")
            .addCallback(AppDatabase.callback)
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideFacilityDao(db: AppDatabase): FacilityDao = db.facilityDao()

    @Provides
    @Singleton
    fun provideCustomFacilityDao(db: AppDatabase): CustomFacilityDao = db.customFacilityDao()
}
