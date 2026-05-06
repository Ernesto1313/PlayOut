package com.ernesto.playout.data.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ernesto.playout.data.model.Instalacion

@Database(entities = [Instalacion::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun instalacionDao(): InstalacionDao

    companion object {
        const val DATABASE_NAME = "playout1.db"

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
                    appContext.assets.open("instalaciones.csv").bufferedReader().use { reader ->
                        val lines = reader.readLines()
                        if (lines.isEmpty()) return
                        val headers = lines.first().split(",").map { it.trim() }
                        var count = 0
                        lines.drop(1).forEach { line ->
                            val values = parseCsvLine(line)
                            val map = headers.zip(values).toMap()
                            try {
                                val nombreSitio = map["nombre_sitio"].orEmpty().replace("'", "''")
                                val foto = map["foto"].orEmpty().replace("'", "''")
                                val categoria = map["categoria"].orEmpty().replace("'", "''")
                                val descripcion = map["descripcion"].orEmpty().replace("'", "''")
                                db.execSQL("""INSERT INTO instalaciones (
                                    fid,nombre_sitio,foto,categoria,descripcion,
                                    estado,agua,asientos,experiencia_uso,longitud,latitud)
                                    VALUES (${map["fid"]},'$nombreSitio',
                                    '$foto','$categoria',
                                    '$descripcion',${map["estado"]},
                                    ${map["agua"]},${map["asientos"]},
                                    ${map["experiencia_uso"]},${map["longitud"]},
                                    ${map["latitud"]})""")
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
