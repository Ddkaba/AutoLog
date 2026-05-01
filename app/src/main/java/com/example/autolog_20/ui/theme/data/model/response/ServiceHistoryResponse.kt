package com.example.autolog_20.ui.theme.data.model.response

import com.example.autolog_20.ui.theme.data.model.CarInfo
import com.example.autolog_20.ui.theme.data.model.PeriodInfo
import com.example.autolog_20.ui.theme.data.model.ServiceRecord
import com.google.gson.annotations.SerializedName

data class ServiceHistoryResponse(
    @SerializedName("car")
    val car: CarInfo,
    @SerializedName("period")
    val period: PeriodInfo,
    @SerializedName("total_cost")
    val totalCost: Double,
    @SerializedName("count")
    val count: Int,
    @SerializedName("services")
    val services: List<ServiceRecord>
)