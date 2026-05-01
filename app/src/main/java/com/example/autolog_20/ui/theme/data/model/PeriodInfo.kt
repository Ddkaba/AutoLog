package com.example.autolog_20.ui.theme.data.model

import com.google.gson.annotations.SerializedName

data class PeriodInfo(
    @SerializedName("type")
    val type: String,
    @SerializedName("from")
    val from: String?,
    @SerializedName("to")
    val to: String?
)
