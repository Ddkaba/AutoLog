package com.example.autolog_20.ui.theme.data.model

data class ExpenseItem(
    val expense_id: Int,
    val type: String,
    val amount: String,
    val description: String?,
    val date: String,
    val mileage: Int?,
    val receipt_photo: String?,
    val category: Category?,
    val category_id: Int?
)