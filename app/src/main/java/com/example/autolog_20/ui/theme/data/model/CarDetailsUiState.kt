package com.example.autolog_20.ui.theme.data.model

sealed interface CarDetailsUiState {
    data object Loading : CarDetailsUiState
    data class Success(val recommendations: List<RecommendationResponse>) : CarDetailsUiState
    data class Error(val message: String) : CarDetailsUiState
}