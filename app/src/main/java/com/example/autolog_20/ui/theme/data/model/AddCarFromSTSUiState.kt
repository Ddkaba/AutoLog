package com.example.autolog_20.ui.theme.data.model

import com.example.autolog_20.ui.theme.data.model.response.CarDetailResponse

interface AddCarFromSTSUiState {
    object Input : AddCarFromSTSUiState
    object Loading : AddCarFromSTSUiState
    data class Preview(val data: STSRecognitionData) : AddCarFromSTSUiState
    object EnterMileage : AddCarFromSTSUiState
    object EnterColor : AddCarFromSTSUiState
    data class Error(val message: String) : AddCarFromSTSUiState
    data class VinAlreadyExistsError(val message: String, val existingCar: ExistingCar) : AddCarFromSTSUiState
    data class PlateAlreadyExistsError(val message: String, val existingCar: CarDetailResponse) : AddCarFromSTSUiState
    object Success : AddCarFromSTSUiState
}