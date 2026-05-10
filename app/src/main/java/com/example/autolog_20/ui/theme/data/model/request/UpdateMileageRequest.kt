package com.example.autolog_20.ui.theme.data.model.request

import com.google.gson.annotations.SerializedName

data class UpdateMileageRequest(
    @SerializedName("date")
    val date: String,
    @SerializedName("mileage")
    val mileage: Int,
    @SerializedName("route")
    val route: String
)