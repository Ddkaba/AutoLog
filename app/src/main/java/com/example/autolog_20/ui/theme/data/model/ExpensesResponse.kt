package com.example.autolog_20.ui.theme.data.model

data class ExpensesResponse(
    val car: CarDetailResponse,
    val filter: FilterInfo,
    val total_spent: Double,
    val count: Int,
    val expenses: List<ExpenseItem>
)
