package com.example.autolog_20.ui.theme.data.model.response

import com.example.autolog_20.ui.theme.data.model.ServicePlace

data class ServicesResponse(
    val services: List<ServicePlace>,
    val total: Int
)
