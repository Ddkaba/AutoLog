package com.example.autolog_20.ui.theme.data.model.request

data class AddExpenseRequest(
    val category: String,
    val amount: Double,
    val date: String,
    val description: String? = null,
    val mileage: Int? = null
)