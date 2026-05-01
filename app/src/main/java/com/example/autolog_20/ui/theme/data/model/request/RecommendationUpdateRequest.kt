package com.example.autolog_20.ui.theme.data.model.request

import com.google.gson.annotations.SerializedName

data class RecommendationUpdateRequest(
    @SerializedName("recommended_mileage")
    val recommendedMileage: Int? = null,
    @SerializedName("service_type")
    val serviceType: String? = null,
    @SerializedName("description")
    val description: String? = null
)