package com.ernesto.playout.di

import android.content.Context
import androidx.room.Room
import com.ernesto.playout.data.db.AppDatabase
import com.ernesto.playout.data.db.InstalacionDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .createFromAsset(AppDatabase.DATABASE_NAME)
            .build()

    @Provides
    @Singleton
    fun provideInstalacionDao(db: AppDatabase): InstalacionDao = db.instalacionDao()
}
