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
        const val DATABASE_NAME = "playout20.db"

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
                Log.d("PlayOut_DB", "onCreate called")
                super.onCreate(db)
                try {
                    appContext.assets.open("facilities.csv").bufferedReader().use { reader ->
                        val allLines = reader.readLines()
                        if (allLines.isEmpty()) return

                        val headers = allLines.first()
                            .trimEnd(';', ' ')
                            .split(",")
                            .map { it.trim() }
                        Log.d("PlayOut_DB", "Headers: $headers")

                        var count = 0
                        val stmt = db.compileStatement("""
                            INSERT INTO facilities
                            (fid, name, sport, description, condition, water, seats,
                             experience, longitude, latitude)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """)

                        allLines.drop(1).forEachIndexed { index, rawLine ->
                            var line = rawLine.trim()
                            if (line.startsWith("\"") && (line.endsWith("\"") || line.endsWith("\";") || line.endsWith("\";"))) {
                                line = line.removePrefix("\"").trimEnd(';', '"', ' ')
                            }
                            line = line.replace("\"\"", "")
                            val values = parseCsvLine(line).map { it.replace("", "\"") }
                            if (values.size < 10) {
                                Log.e("PlayOut_DB", "Row $index has only ${values.size} values, skipping")
                                return@forEachIndexed
                            }
                            val map = headers.zip(values).toMap()
                            Log.d("PlayOut_DB", "Row $index: fid=${map["fid"]} name=${map["name"]} sport=${map["sport"]}")
                            try {
                                stmt.clearBindings()
                                stmt.bindLong(1, map["fid"]?.trim()?.toLongOrNull() ?: return@forEachIndexed)
                                stmt.bindString(2, map["name"]?.trim().orEmpty())
                                stmt.bindString(3, map["sport"]?.trim().orEmpty())
                                stmt.bindString(4, map["description"]?.trim().orEmpty())
                                stmt.bindLong(5, map["condition"]?.trim()?.toLongOrNull() ?: 0)
                                stmt.bindLong(6, map["water"]?.trim()?.toLongOrNull() ?: 0)
                                stmt.bindLong(7, map["seats"]?.trim()?.toLongOrNull() ?: 0)
                                stmt.bindLong(8, map["experience"]?.trim()?.toLongOrNull() ?: 0)
                                stmt.bindDouble(9, map["longitude"]?.trim()?.toDoubleOrNull() ?: return@forEachIndexed)
                                stmt.bindDouble(10, map["latitude"]?.trim()?.toDoubleOrNull() ?: return@forEachIndexed)
                                stmt.executeInsert()
                                count++
                                Log.d("PlayOut_DB", "Inserted row $count: ${map["name"]}")
                            } catch (e: Exception) {
                                Log.e("PlayOut_DB", "Row $index insert failed: ${e.message}")
                            }
                        }
                        Log.d("PlayOut_DB", "Import complete. Total: $count")
                    }
                } catch (e: Exception) {
                    Log.e("PlayOut_DB", "Fatal CSV error: ${e.message}")
                }
            }
        }
    }
}
