package com.ernesto.playout.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "instalaciones")
data class Instalacion(
    @PrimaryKey val fid: Int,
    val nombre_sitio: String?,
    val foto: String?,
    val categoria: String?,
    val descripcion: String?,
    val estado: Int?,
    val agua: Int?,
    val asientos: Int?,
    val experiencia_uso: Int?,
    val longitud: Double?,
    val latitud: Double?
)
