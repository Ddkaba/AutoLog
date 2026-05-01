package com.example.autolog_20.ui.theme.data.model.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autolog_20.ui.theme.data.api.AuthApi
import com.example.autolog_20.ui.theme.data.model.MaintenanceUiState
import com.example.autolog_20.ui.theme.data.model.ServiceRecord
import com.example.autolog_20.ui.theme.data.model.response.RecommendationResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class MaintenanceViewModel(
    private val authApi: AuthApi,
    private val numberPlate: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<MaintenanceUiState>(MaintenanceUiState.Loading)
    val uiState: StateFlow<MaintenanceUiState> = _uiState.asStateFlow()

    private val _currentMileage = MutableStateFlow<Int?>(null)
    val currentMileage: StateFlow<Int?> = _currentMileage.asStateFlow()

    private var currentCarId: Int? = null

    init {
        loadCarId()
    }

    private fun loadCarId() {
        viewModelScope.launch {
            try {
                val carResponse = authApi.getCarByPlate(numberPlate)
                if (carResponse.isSuccessful) {
                    currentCarId = carResponse.body()?.carId
                    if (currentCarId != null) {
                        loadPlannedRecommendations()
                        loadCurrentMileage()
                    } else {
                        _uiState.value = MaintenanceUiState.Error("Не удалось найти автомобиль")
                    }
                } else {
                    _uiState.value = MaintenanceUiState.Error("Не удалось найти автомобиль")
                }
            } catch (e: Exception) {
                _uiState.value = MaintenanceUiState.Error("Ошибка сети: ${e.localizedMessage}")
            }
        }
    }

    fun loadPlannedRecommendations() {
        val carId = currentCarId ?: return
        viewModelScope.launch {
            _uiState.value = MaintenanceUiState.Loading
            try {
                val response = authApi.getRecommendations(carId)
                if (response.isSuccessful) {
                    val recommendations = response.body() ?: emptyList()
                    // Сортируем по пробегу (от меньшего к большему)
                    val sortedRecommendations = recommendations.sortedBy { it.recommendedMileage }
                    _uiState.value = MaintenanceUiState.Planned(sortedRecommendations)
                } else {
                    _uiState.value = MaintenanceUiState.Error("Ошибка загрузки: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = MaintenanceUiState.Error("Сетевая ошибка: ${e.localizedMessage}")
            }
        }
    }

    fun loadCompletedServices() {
        val carId = currentCarId ?: return
        viewModelScope.launch {
            _uiState.value = MaintenanceUiState.Loading
            try {
                val response = authApi.getServiceHistory(carId)
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        // Сортируем по дате (от новых к старым)
                        val sortedServices = data.services.sortedByDescending { it.date }
                        _uiState.value = MaintenanceUiState.Completed(sortedServices)
                    } else {
                        _uiState.value = MaintenanceUiState.Completed(emptyList())
                    }
                } else {
                    _uiState.value = MaintenanceUiState.Error("Ошибка загрузки: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = MaintenanceUiState.Error("Сетевая ошибка: ${e.localizedMessage}")
            }
        }
    }

    private fun loadCurrentMileage() {
        val carId = currentCarId ?: return
        viewModelScope.launch {
            try {
                val response = authApi.getMileage(carId, "all")
                if (response.isSuccessful) {
                    val data = response.body()
                    val lastLog = data?.logs?.maxByOrNull { it.date }
                    _currentMileage.value = lastLog?.mileage
                }
            } catch (e: Exception) {
                // Ошибка загрузки пробега - не критично
            }
        }
    }

    fun getCurrentMileage(): Int? = _currentMileage.value

    fun deleteService(
        service: ServiceRecord,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                // TODO: Реализовать API запрос на удаление
                // val response = authApi.deleteService(service.recordId)
                // if (response.isSuccessful) {
                //     loadCompletedServices()
                //     onSuccess()
                // } else {
                //     onError("Ошибка удаления: ${response.code()}")
                // }

                // Временное решение
                loadCompletedServices()
                onSuccess()
            } catch (e: Exception) {
                onError("Сетевая ошибка: ${e.localizedMessage}")
            }
        }
    }

    fun updateService(
        service: ServiceRecord,
        date: String,
        mileage: Int,
        cost: Double,
        notes: String?,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                // TODO: Реализовать API запрос на обновление
                // val response = authApi.updateService(service.recordId, date, mileage, cost, notes)
                // if (response.isSuccessful) {
                //     loadCompletedServices()
                //     onSuccess()
                // } else {
                //     onError("Ошибка обновления: ${response.code()}")
                // }

                // Временное решение
                loadCompletedServices()
                onSuccess()
            } catch (e: Exception) {
                onError("Сетевая ошибка: ${e.localizedMessage}")
            }
        }
    }

    fun updateRecommendation(
        recommendation: RecommendationResponse,
        recommendedMileage: Int,
        description: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                // TODO: Реализовать API запрос на обновление рекомендации
                // val response = authApi.updateRecommendation(
                //     recommendation.recommendationId,
                //     recommendedMileage,
                //     description
                // )
                // if (response.isSuccessful) {
                //     loadPlannedRecommendations()
                //     onSuccess()
                // } else {
                //     onError("Ошибка обновления: ${response.code()}")
                // }

                // Временное решение
                loadPlannedRecommendations()
                onSuccess()
            } catch (e: Exception) {
                onError("Сетевая ошибка: ${e.localizedMessage}")
            }
        }
    }
}