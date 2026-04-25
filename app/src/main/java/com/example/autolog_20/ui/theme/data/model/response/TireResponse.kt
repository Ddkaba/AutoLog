package com.example.autolog_20.ui.theme.data.model.response

import com.example.autolog_20.ui.theme.data.model.Coordinates
import com.google.gson.annotations.SerializedName

data class TireResponse(
    val recommendation: String,
    val reason: String,
    @SerializedName("current_tires")
    val currentTires: String,
    @SerializedName("should_change_to")
    val shouldChangeTo: String,
    @SerializedName("avg_week_temp")
    val avgWeekTemp: Double,
    val coordinates: Coordinates
)