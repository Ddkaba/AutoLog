package com.example.autolog_20.ui.theme.data.model.request

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody

data class CreateServiceRequest(
    @SerializedName("service_type")
    val serviceType: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("mileage")
    val mileage: Int,
    @SerializedName("cost")
    val cost: Double,
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("receipt_photo")
    val receiptPhoto: MultipartBody.Part? = null,
    @SerializedName("recommendation_id")
    val recommendationId: Int? = null
)
