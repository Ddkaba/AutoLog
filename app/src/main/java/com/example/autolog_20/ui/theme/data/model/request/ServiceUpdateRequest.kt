package com.example.autolog_20.ui.theme.data.model.request

import com.google.gson.annotations.SerializedName

data class ServiceUpdateRequest(
    @SerializedName("cost")
    val cost: Double? = null,
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("mileage")
    val mileage: Int? = null
)