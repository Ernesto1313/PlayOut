package com.ernesto.playout.data.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ernesto.playout.data.model.Instalacion
import com.ernesto.playout.data.model.InstalacionCustom

@Database(entities = [Instalacion::class, InstalacionCustom::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun instalacionDao(): InstalacionDao
    abstract fun instalacionCustomDao(): InstalacionCustomDao

    companion object {
        const val DATABASE_NAME = "playout3.db"

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
                super.onCreate(db)
                try {
                    appContext.assets.open("facilities.csv").bufferedReader().use { reader ->
                        val lines = reader.readLines()
                        if (lines.isEmpty()) return
                        val headers = lines.first().split(",").map { it.trim() }
                        var count = 0
                        lines.drop(1).forEach { line ->
                            val values = parseCsvLine(line)
                            val map = headers.zip(values).toMap()
                            try {
                                val nombreSitio = map["name"].orEmpty().replace("'", "''")
                                val categoria = map["sport"].orEmpty().replace("'", "''")
                                val descripcion = map["description"].orEmpty().replace("'", "''")
                                db.execSQL("""INSERT INTO instalaciones (
                                    fid,nombre_sitio,categoria,descripcion,
                                    estado,agua,asientos,experiencia_uso,longitud,latitud)
                                    VALUES (${map["fid"]},'$nombreSitio',
                                    '$categoria',
                                    '$descripcion',${map["condition"]},
                                    ${map["water"]},${map["seats"]},
                                    ${map["expirience"]},${map["longitude"]},
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
