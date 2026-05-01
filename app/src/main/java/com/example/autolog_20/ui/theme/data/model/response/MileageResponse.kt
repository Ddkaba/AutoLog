package com.example.autolog_20.ui.theme.data.model.response

import com.example.autolog_20.ui.theme.data.model.CarInfo
import com.example.autolog_20.ui.theme.data.model.PeriodInfo
import com.google.gson.annotations.SerializedName

data class MileageResponse(
    @SerializedName("car")
    val car: CarInfo,
    @SerializedName("period")
    val period: PeriodInfo,
    @SerializedName("total_distance_km")
    val totalDistanceKm: Double?,
    @SerializedName("count")
    val count: Int,
    @SerializedName("logs")
    val logs: List<MileageLog>
)

data class MileageLog(
    @SerializedName("type")
    val type: String,
    @SerializedName("log_id")
    val logId: Int,
    @SerializedName("date")
    val date: String,
    @SerializedName("mileage")
    val mileage: Int,
    @SerializedName("route")
    val route: String
)
