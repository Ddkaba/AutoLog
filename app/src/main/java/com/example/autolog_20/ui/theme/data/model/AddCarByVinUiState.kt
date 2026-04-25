package com.example.autolog_20.ui.theme.data.model

import com.example.autolog_20.ui.theme.data.model.response.CarDetailResponse

sealed interface AddCarByVinUiState {
    data object Input : AddCarByVinUiState
    data object Loading : AddCarByVinUiState
    data class Preview(val data: VinPreviewData) : AddCarByVinUiState
    object EnterColor : AddCarByVinUiState
    data object EnterMileage : AddCarByVinUiState
    data class VinAlreadyExistsError(val message: String, val existingCar: ExistingCar) : AddCarByVinUiState
    data class PlateAlreadyExistsError(val message: String, val existingCar: CarDetailResponse) : AddCarByVinUiState
    data object Success : AddCarByVinUiState
    data class Error(val message: String) : AddCarByVinUiState
}