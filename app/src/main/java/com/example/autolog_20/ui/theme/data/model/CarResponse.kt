package com.example.autolog_20.ui.theme.data.model

import com.google.gson.annotations.SerializedName

data class CarResponse(
    val id: Int,
    val brand: String,
    val model: String,
    @SerializedName("number_plate")
    val numberPlate: String,
    val isOwner: Boolean
)
