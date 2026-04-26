package com.example.autolog_20.ui.theme.data.model

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("category_id")
    val categoryId: Int,
    val name: String,
    val description: String?
)