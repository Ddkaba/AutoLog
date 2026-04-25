package com.example.autolog_20.ui.theme.data.model.response

import com.google.gson.annotations.SerializedName

data class CarDetailResponse(
    @SerializedName("car_id")
    val carId: Int,
    val vin: String,
    val brand: String,
    val model: String,
    @SerializedName("year_of_manufacture")
    val yearOfManufacture: Int,
    val color: String,
    @SerializedName("number_plate")
    val numberPlate: String
)