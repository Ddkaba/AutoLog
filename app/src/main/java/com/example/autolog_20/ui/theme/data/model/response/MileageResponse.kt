package com.example.autolog_20.ui.theme.data.model.response

import com.example.autolog_20.ui.theme.data.model.CarInfo
import com.example.autolog_20.ui.theme.data.model.MileageLog
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

