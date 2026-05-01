package com.example.autolog_20.ui.theme.data.model

import com.google.gson.annotations.SerializedName

data class CarInfo(
    @SerializedName("car_id")
    val carId: Int,
    @SerializedName("brand")
    val brand: String,
    @SerializedName("model")
    val model: String,
    @SerializedName("number_plate")
    val numberPlate: String
)
