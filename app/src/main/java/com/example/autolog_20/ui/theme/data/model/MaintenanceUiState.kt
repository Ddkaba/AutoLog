package com.example.autolog_20.ui.theme.data.model

import com.example.autolog_20.ui.theme.data.model.response.RecommendationResponse

interface MaintenanceUiState {
    object Loading : MaintenanceUiState
    data class Planned(val recommendations: List<RecommendationResponse>) : MaintenanceUiState
    data class Completed(val services: List<ServiceRecord>) : MaintenanceUiState
    data class Error(val message: String) : MaintenanceUiState
}