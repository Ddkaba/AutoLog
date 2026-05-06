package com.example.autolog_20.ui.theme.data.model.request

import com.google.gson.annotations.SerializedName

data class RouteRequest(
    @SerializedName("start_lat")
    val startLat: Double,
    @SerializedName("start_lon")
    val startLon: Double,
    @SerializedName("end_lat")
    val endLat: Double,
    @SerializedName("end_lon")
    val endLon: Double
)
