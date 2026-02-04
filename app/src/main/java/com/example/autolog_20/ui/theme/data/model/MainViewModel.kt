package com.example.autolog_20.ui.theme.data.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.autolog_20.ui.theme.data.api.AuthApi
import com.example.autolog_20.ui.theme.data.locale.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class MainViewModel(
    private val authApi: AuthApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

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

    fun logout(navController: NavController) {
        TokenManager.clearTokens()
        navController.navigate("login") {
            popUpTo(0) { inclusive = true }
        }
    }
}

sealed interface MainUiState {
    data object Loading : MainUiState
    data object Empty : MainUiState
    data class Success(val cars: List<CarResponse>) : MainUiState
    data class Error(val message: String) : MainUiState
    data object Unauthorized : MainUiState
}