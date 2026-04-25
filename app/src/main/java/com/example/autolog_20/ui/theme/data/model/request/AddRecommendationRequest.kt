package com.example.autolog_20.ui.theme.data.model.request

import com.google.gson.annotations.SerializedName

data class AddRecommendationRequest(
    @SerializedName("car_id")
    val carId: Int,
    @SerializedName("service_type")
    val serviceType: String,
    @SerializedName("recommended_mileage")
    val recommendedMileage: Int,
    @SerializedName("description")
    val description: String
)
