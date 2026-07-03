package com.ernesto.playout.data.model

data class Proposal(
    val id: String = "",
    val name: String = "",
    val sport: String = "",
    val description: String = "",
    val condition: Int = 0,
    val water: Int = 0,
    val seats: Int = 0,
    val experience: Int = 0,
    val longitude: Double = 0.0,
    val latitude: Double = 0.0,
    val photoUrls: List<String> = emptyList(),
    val neighbourhood: String = "",
    val status: String = "pending",
    val localFid: Int = 0
)
