package com.example.autolog_20.ui.theme.data.model

data class ServicePlace(
    val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lon: Double,
    val rating: Double?,
    val ratingCount: Int?,
    val phone: String?,
    val website: String?,
    val workingHours: Map<String, String>?,
    val distance: Double,
    val photoUrl: String?
)
