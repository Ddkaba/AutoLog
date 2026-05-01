package com.example.autolog_20.ui.theme.data.model

import com.google.gson.annotations.SerializedName

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

