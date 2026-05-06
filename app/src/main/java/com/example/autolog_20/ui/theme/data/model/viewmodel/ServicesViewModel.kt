package com.example.autolog_20.ui.theme.data.model.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autolog_20.ui.theme.data.api.AuthApi
import com.example.autolog_20.ui.theme.data.model.RouteInfo
import com.example.autolog_20.ui.theme.data.model.ServicePlace
import com.example.autolog_20.ui.theme.data.model.ServicesUiState
import com.example.autolog_20.ui.theme.data.model.request.RouteRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServicesViewModel(
    private val authApi: AuthApi
) : ViewModel() {

    // Используем ServicesUiState как общий тип
    private val _uiState = MutableStateFlow<ServicesUiState>(ServicesUiState.Loading)
    val uiState: StateFlow<ServicesUiState> = _uiState.asStateFlow()

    private val _selectedService = MutableStateFlow<ServicePlace?>(null)
    val selectedService: StateFlow<ServicePlace?> = _selectedService.asStateFlow()

    private val _routeInfo = MutableStateFlow<RouteInfo?>(null)
    val routeInfo: StateFlow<RouteInfo?> = _routeInfo.asStateFlow()

    fun searchServices(lat: Double, lon: Double, query: String = "автосервис", radius: Int = 5000) {
        viewModelScope.launch {
            _uiState.value = ServicesUiState.Loading

            try {
                val response = authApi.searchServices(lat, lon, query, radius)

                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = ServicesUiState.Success(response.body()!!.services)
                } else {
                    _uiState.value = ServicesUiState.Error("Ошибка загрузки: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = ServicesUiState.Error("Сетевая ошибка: ${e.localizedMessage}")
            }
        }
    }

    fun selectService(service: ServicePlace) {
        _selectedService.value = service
    }

    fun clearSelectedService() {
        _selectedService.value = null
        _routeInfo.value = null
    }

    fun clearRouteInfo() {
        _routeInfo.value = null
    }

    fun buildRoute(startLat: Double, startLon: Double, endLat: Double, endLon: Double) {
        viewModelScope.launch {
            try {
                val request = RouteRequest(
                    startLat = startLat,
                    startLon = startLon,
                    endLat = endLat,
                    endLon = endLon
                )

                val response = authApi.buildRoute(request)

                if (response.isSuccessful && response.body() != null) {
                    _routeInfo.value = response.body()
                } else {
                    // Обработка ошибки
                }
            } catch (e: Exception) {
                // Обработка ошибки
            }
        }
    }
}

