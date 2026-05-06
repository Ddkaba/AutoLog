package com.example.autolog_20.ui.theme.data.model

interface ServicesUiState {
    object Loading : ServicesUiState
    data class Success(val services: List<ServicePlace>) : ServicesUiState
    data class Error(val message: String) : ServicesUiState
}