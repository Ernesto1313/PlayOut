package com.ernesto.playout.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "facilities")
data class Facility(
    @PrimaryKey val fid: Int,
    val name: String?,
    val photo: String?,
    val sport: String?,
    val description: String?,
    val condition: Int?,
    val water: Int?,
    val seats: Int?,
    val experience: Int?,
    val longitude: Double?,
    val latitude: Double?,
    val photoUrlsJson: String? = null,
    val neighbourhood: String? = null
)
