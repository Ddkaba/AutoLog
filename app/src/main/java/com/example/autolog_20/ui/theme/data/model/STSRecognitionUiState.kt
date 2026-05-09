package com.example.autolog_20.ui.theme.data.model

interface STSRecognitionUiState {
    object Idle : STSRecognitionUiState
    object Loading : STSRecognitionUiState
    data class Success(val data: STSRecognitionData) : STSRecognitionUiState
    data class Error(val message: String) : STSRecognitionUiState
}