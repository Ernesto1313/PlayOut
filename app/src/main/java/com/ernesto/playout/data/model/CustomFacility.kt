package com.ernesto.playout.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_facilities")
data class CustomFacility(
    @PrimaryKey val fid: Int = 0,
    val name: String = "custom",
    val photo: String? = null,
    val sport: String?,
    val description: String?,
    val condition: Int?,
    val water: Int?,
    val seats: Int?,
    val experience: Int?,
    val longitude: Double?,
    val latitude: Double?
)
