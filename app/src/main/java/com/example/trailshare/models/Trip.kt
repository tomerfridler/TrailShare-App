package com.example.trailshare

data class Trip(
    var title: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var uploadDate: Long = 0L,
    var ownerEmail: String = "",
    var ownerUid: String = ""
)


