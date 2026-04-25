package com.example.autolog_20.ui.theme.data.model.response

import com.example.autolog_20.ui.theme.data.model.ExistingCar

data class VinCheckResponse(
    val exists: Boolean,
    val message: String,
    val car: ExistingCar? = null
)