package com.waseem.locationtracking.data

data class UserLocation(
    val userId: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val email: String = ""
)
