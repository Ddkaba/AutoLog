package com.example.autolog_20.ui.theme.data.model

data class MileageUiState(
    val isLoading: Boolean = false,
    val logs: List<MileageLog> = emptyList(),
    val currentMileage: Int? = null,
    val selectedPeriod: String = "all",
    val customFrom: String? = null,
    val customTo: String? = null,
    val error: String? = null
)