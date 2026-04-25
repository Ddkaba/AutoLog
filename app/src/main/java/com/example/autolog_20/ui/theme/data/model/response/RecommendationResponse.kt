package com.example.autolog_20.ui.theme.data.model.response

import com.example.autolog_20.ui.theme.data.model.response.CarResponse
import com.google.gson.annotations.SerializedName

data class RecommendationResponse(
    @SerializedName("recommendation_id")
    val recommendationId: Int,
    @SerializedName("car_id")
    val carId: CarResponse,
    @SerializedName("service_type")
    val serviceType: String,
    @SerializedName("recommended_mileage")
    val recommendedMileage: Int,
    val description: String
)