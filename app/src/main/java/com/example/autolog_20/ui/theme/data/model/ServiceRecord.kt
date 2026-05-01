package com.example.autolog_20.ui.theme.data.model

import com.google.gson.annotations.SerializedName

data class ServiceRecord(
    @SerializedName("type")
    val type: String,
    @SerializedName("record_id")
    val recordId: Int,
    @SerializedName("service_type")
    val serviceType: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("mileage")
    val mileage: Int,
    @SerializedName("cost")
    val cost: String,
    @SerializedName("notes")
    val notes: String?,
    @SerializedName("receipt_photo")
    val receiptPhoto: String?,
    @SerializedName("expenses_id")
    val expensesId: Int?
)
