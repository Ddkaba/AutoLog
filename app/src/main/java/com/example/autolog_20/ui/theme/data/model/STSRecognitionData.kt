package com.example.autolog_20.ui.theme.data.model

import com.google.gson.annotations.SerializedName

data class STSRecognitionData(
    val vin: String,
    val brand: String,
    val model: String,
    val year: Int,
    val color: String,
    @SerializedName("number_plate")
    val numberPlate: String
)
