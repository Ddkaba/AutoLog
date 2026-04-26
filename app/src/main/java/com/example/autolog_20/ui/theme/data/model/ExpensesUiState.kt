package com.example.autolog_20.ui.theme.data.model

sealed interface ExpensesUiState {
    data object Loading : ExpensesUiState
    data class Success(
        val totalSpent: Double,
        val expenses: List<ExpenseItem>
    ) : ExpensesUiState

    data class Error(val message: String) : ExpensesUiState
}