package com.ernesto.playout.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ernesto.playout.data.model.CustomFacility
import com.ernesto.playout.data.model.Facility

@Database(entities = [Facility::class, CustomFacility::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun facilityDao(): FacilityDao
    abstract fun customFacilityDao(): CustomFacilityDao

    companion object {
        const val DATABASE_NAME = "playout22.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS instalaciones_custom (
                        fid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nombre_sitio TEXT NOT NULL DEFAULT 'custom',
                        foto TEXT,
                        categoria TEXT,
                        descripcion TEXT,
                        estado INTEGER,
                        agua INTEGER,
                        asientos INTEGER,
                        experiencia_uso INTEGER,
                        longitud REAL,
                        latitud REAL
                    )
                """)
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS instalaciones_custom")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS instalaciones_custom (
                        fid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nombre_sitio TEXT NOT NULL DEFAULT 'custom',
                        foto TEXT,
                        categoria TEXT,
                        descripcion TEXT,
                        estado INTEGER,
                        agua INTEGER,
                        asientos INTEGER,
                        experiencia_uso INTEGER,
                        longitud REAL,
                        latitud REAL
                    )
                """)
                database.execSQL("INSERT INTO instalaciones_custom (fid) VALUES (9999)")
                database.execSQL("DELETE FROM instalaciones_custom WHERE fid = 9999")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("INSERT INTO instalaciones_custom (fid) VALUES (9999)")
                database.execSQL("DELETE FROM instalaciones_custom WHERE fid = 9999")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No schema change, fid management moved to repository
            }
        }

        lateinit var appContext: Context
    }
}
