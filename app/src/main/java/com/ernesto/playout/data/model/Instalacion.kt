package com.ernesto.playout.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "instalaciones")
data class Instalacion(
    @PrimaryKey val fid: Int,
    val nombre_sitio: String,
    val categoria: String,
    val descripcion: String,
    val estado: Int,
    val experiencia_uso: Int,
    val agua: Boolean,
    val asientos: Boolean,
    val foto: String,
    val latitud: Double,
    val longitud: Double
)
