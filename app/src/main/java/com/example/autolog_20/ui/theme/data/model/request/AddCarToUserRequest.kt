package com.example.autolog_20.ui.theme.data.model.request

import com.google.gson.annotations.SerializedName

data class AddCarToUserRequest(
    @SerializedName("car_id")
    val carId: Int,
    @SerializedName("is_owner")
    val isOwner: Boolean
)
