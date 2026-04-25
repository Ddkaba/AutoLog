package com.example.autolog_20.ui.theme.data.model.response

import com.google.gson.annotations.SerializedName

data class CarAddResponse(
    @SerializedName("car_id")
    val carId: Int,
    val message: String
)