package com.ernesto.playout.data.db

import android.content.Context
import android.util.Log
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
        const val DATABASE_NAME = "playout17.db"

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

        fun parseCsvLine(line: String): List<String> {
            val result = mutableListOf<String>()
            var current = StringBuilder()
            var inQuotes = false
            for (char in line) {
                when {
                    char == '"' -> inQuotes = !inQuotes
                    char == ',' && !inQuotes -> {
                        result.add(current.toString().trim())
                        current = StringBuilder()
                    }
                    else -> current.append(char)
                }
            }
            result.add(current.toString().trim())
            return result
        }

        val callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                Log.d("PlayOut_DB", "onCreate called - creating fresh database")
                super.onCreate(db)
                try {
                    appContext.assets.open("facilities.csv").bufferedReader().use { reader ->
                        val lines = reader.readLines()
                        if (lines.isEmpty()) return
                        val headers = lines.first()
                            .trimEnd(';')
                            .split(",")
                            .map { it.trim() }
                        var count = 0
                        lines.drop(1).forEach { rawLine ->
                            val line = rawLine.trim().trimStart('"').trimEnd('"', ';')
                            if (line.isBlank()) return@forEach
                            val values = parseCsvLine(line)
                            val map = headers.zip(values).toMap()
                            try {
                                val name = map["name"].orEmpty().replace("'", "''")
                                val sport = map["sport"].orEmpty().replace("'", "''")
                                val description = map["description"].orEmpty().replace("'", "''")
                                db.execSQL("""INSERT INTO facilities (
                                    fid,name,sport,description,
                                    condition,water,seats,experience,longitude,latitude)
                                    VALUES (${map["fid"]},'$name',
                                    '$sport',
                                    '$description',${map["condition"]},
                                    ${map["water"]},${map["seats"]},
                                    ${map["experience"]},${map["longitude"]},
                                    ${map["latitude"]})""")
                                count++
                                Log.d("PlayOut_DB", "Inserted row $count")
                            } catch (e: Exception) {
                                Log.e("PlayOut_DB", "Row failed: ${e.message}")
                            }
                        }
                        Log.d("PlayOut_DB", "Import complete. Total rows: $count")
                    }
                } catch (e: Exception) {
                    Log.e("PlayOut_DB", "CSV import failed: ${e.message}")
                }
            }
        }
    }
}
