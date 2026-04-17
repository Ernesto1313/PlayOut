package com.ernesto.playout.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ernesto.playout.data.model.Instalacion

@Database(entities = [Instalacion::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun instalacionDao(): InstalacionDao

    companion object {
        const val DATABASE_NAME = "playout.db"
    }
}
