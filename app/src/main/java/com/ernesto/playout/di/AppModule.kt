package com.ernesto.playout.di

import android.content.Context
import androidx.room.Room
import com.ernesto.playout.data.db.AppDatabase
import com.ernesto.playout.data.db.CustomFacilityDao
import com.ernesto.playout.data.db.FacilityDao
import com.ernesto.playout.data.remote.AuthDataSource
import com.ernesto.playout.data.remote.FirebaseSyncManager
import com.ernesto.playout.data.remote.FirestoreDataSource
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
        return Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8
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

    @Provides
    @Singleton
    fun provideFirestoreDataSource(): FirestoreDataSource = FirestoreDataSource()

    @Provides
    @Singleton
    fun provideAuthDataSource(): AuthDataSource = AuthDataSource()

    @Provides
    @Singleton
    fun provideFirebaseSyncManager(
        firestoreDataSource: FirestoreDataSource,
        facilityDao: FacilityDao,
        customFacilityDao: CustomFacilityDao
    ): FirebaseSyncManager = FirebaseSyncManager(firestoreDataSource, facilityDao, customFacilityDao)
}
