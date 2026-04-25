package com.example.autolog_20.ui.theme.data.model.response

import kotlinx.serialization.SerialName

data class AddCarToUserResponse(
    val message: String,
    @SerialName("userCarId")
    val userCarId: Int
)
