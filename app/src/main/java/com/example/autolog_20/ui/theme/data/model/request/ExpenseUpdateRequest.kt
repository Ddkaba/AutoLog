package com.example.autolog_20.ui.theme.data.model.request


import com.google.gson.annotations.SerializedName

data class ExpenseUpdateRequest(
    @SerializedName("amount")
    val amount: Double? = null,
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("category_id")
    val categoryId: Int? = null
)
