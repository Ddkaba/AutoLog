package com.example.autolog_20.ui.theme.data.model.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val access: String,
    @SerializedName("user_id")
    val userId: Int,
    val username: String
)