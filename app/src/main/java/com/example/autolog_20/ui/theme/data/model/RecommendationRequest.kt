package com.example.autolog_20.ui.theme.data.model

import com.google.gson.annotations.SerializedName

data class RecommendationRequest(
    @SerializedName("car_id")
    val carId: Int,
    @SerializedName("service_type")
    val serviceType: String,
    @SerializedName("recommended_mileage")
    val recommendedMileage: Int,
    val description: String
)
