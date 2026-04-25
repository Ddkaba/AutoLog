package com.example.autolog_20.ui.theme.data.model

sealed interface AddCarManualUiState {
    object Input : AddCarManualUiState
    object Loading : AddCarManualUiState
    data class Preview(val data: ManualCarPreview) : AddCarManualUiState
    object EnterMileage : AddCarManualUiState
    object EnterColor : AddCarManualUiState
    data class Error(val message: String) : AddCarManualUiState
    data class VinAlreadyExistsError(val message: String, val existingCar: ExistingCar) : AddCarManualUiState
    data class DrivingSurveyState(val survey: DrivingSurvey) : AddCarManualUiState
    data class SurveySummaryState(val summary: SurveySummary) : AddCarManualUiState
    object Success : AddCarManualUiState
}
