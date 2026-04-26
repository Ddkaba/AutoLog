package com.example.autolog_20.ui.theme.data.model.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.autolog_20.ui.theme.data.api.AuthApi
import com.example.autolog_20.ui.theme.data.locale.TokenManager
import com.example.autolog_20.ui.theme.data.model.MainUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class MainViewModel(
    private val authApi: AuthApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    init {
        loadCars()
    }

    fun loadCars() {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading

            try {
                val response = authApi.getMyCars()

                if (response.isSuccessful) {
                    val cars = response.body() ?: emptyList()
                    _uiState.value = if (cars.isEmpty()) {
                        MainUiState.Empty
                    } else {
                        MainUiState.Success(cars)
                    }
                } else {
                    if (response.code() == 401) {
                        _uiState.value = MainUiState.Unauthorized
                    } else {
                        _uiState.value = MainUiState.Error("Ошибка загрузки: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error("Сетевая ошибка: ${e.localizedMessage}")
            }
        }
    }

    fun deleteCarByNumberPlate(
        numberPlate: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isDeleting.value = true
            try {
                val carResponse = authApi.getCarByPlate(numberPlate)

                if (!carResponse.isSuccessful) {
                    onError("Автомобиль с номером $numberPlate не найден")
                    return@launch
                }

                val carDetails = carResponse.body()
                if (carDetails == null) {
                    onError("Не удалось получить данные автомобиля")
                    return@launch
                }

                val carId = carDetails.carId

                val deleteResponse = authApi.deleteCar(carId)

                if (deleteResponse.isSuccessful) {
                    loadCars()
                    onSuccess()
                } else {
                    val errorMessage = when (deleteResponse.code()) {
                        401 -> "Не авторизован"
                        403 -> "Нет прав на удаление"
                        404 -> "Автомобиль не найден"
                        else -> "Ошибка удаления: ${deleteResponse.code()}"
                    }
                    onError(errorMessage)
                }
            } catch (e: Exception) {
                onError("Сетевая ошибка: ${e.localizedMessage}")
            } finally {
                _isDeleting.value = false
            }
        }
    }

    fun logout(navController: NavController) {
        TokenManager.clearTokens()
        navController.navigate("login") {
            popUpTo(0) { inclusive = true }
        }
    }
}

