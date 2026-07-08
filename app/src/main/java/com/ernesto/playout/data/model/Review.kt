package com.ernesto.playout.data.model

data class Review(
    val id: String = "",
    val facilityFid: Int = 0,
    val userId: String = "",
    val username: String = "",
    val stars: Int = 0,
    val condition: Int = 0,  // 1 Good, 2 Fair, 3 Broken
    val comment: String = "",
    val photoUrls: List<String> = emptyList(),
    val timestamp: Long = 0L,
    val isInitial: Boolean = false  // true for the creator's initial review
)
