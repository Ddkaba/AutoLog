package com.example.autolog_20.ui.theme.data.model.response

import com.example.autolog_20.ui.theme.data.model.ExpenseItem
import com.example.autolog_20.ui.theme.data.model.FilterInfo
import com.google.gson.annotations.SerializedName

data class ExpensesResponse(
    val car: CarDetailResponse,
    val filter: FilterInfo,
    @SerializedName("total_spent")
    val totalSpent: Double,
    val count: Int,
    val expenses: List<ExpenseItem>
)