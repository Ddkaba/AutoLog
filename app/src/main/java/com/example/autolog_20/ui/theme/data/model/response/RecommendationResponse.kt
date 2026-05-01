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
    @SerializedName("description")
    val description: String,
    @SerializedName("next_recommended_mileage")
    val nextRecommendedMileage: Int?,
    @SerializedName("last_service_record_id")
    val lastServiceRecordId: Int?
)