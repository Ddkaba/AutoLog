package com.example.autolog_20.ui.theme.data.model

import com.example.autolog_20.ui.theme.data.model.response.CarResponse

sealed interface MainUiState {
    data object Loading : MainUiState
    data object Empty : MainUiState
    data class Success(val cars: List<CarResponse>) : MainUiState
    data class Error(val message: String) : MainUiState
    data object Unauthorized : MainUiState
}