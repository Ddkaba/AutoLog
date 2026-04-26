package com.example.autolog_20.ui.theme.data.model.request

import com.google.gson.annotations.SerializedName

data class CarUpdateRequest(
    @SerializedName("color")
    val color: String,
    @SerializedName("number_plate")
    val numberPlate: String
)
