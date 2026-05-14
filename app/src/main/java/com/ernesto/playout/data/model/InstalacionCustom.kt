package com.ernesto.playout.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "instalaciones_custom")
data class InstalacionCustom(
    @PrimaryKey(autoGenerate = true) val fid: Int = 0,
    val nombre_sitio: String = "custom",
    val foto: String? = null,
    val categoria: String?,
    val descripcion: String?,
    val estado: Int?,
    val agua: Int?,
    val asientos: Int?,
    val experiencia_uso: Int?,
    val longitud: Double?,
    val latitud: Double?
)
