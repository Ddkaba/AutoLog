package com.example.autolog_20.ui.theme.data.model

import com.google.gson.annotations.SerializedName

data class ExpenseItem(
    @SerializedName("expense_id")
    val expenseId: Int,
    val type: String,
    val amount: String,
    val description: String?,
    val date: String,
    val mileage: Int?,
    @SerializedName("receipt_photo")
    val receiptPhoto: String?,
    val category: Category?,
    @SerializedName("category_id")
    val categoryId: Int?
)