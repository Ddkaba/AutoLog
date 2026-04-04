package com.example.autolog_20.ui.theme.data.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autolog_20.ui.theme.data.api.AuthApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExpensesViewModel(
    private val authApi: AuthApi,
    private val numberPlate: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExpensesUiState>(ExpensesUiState.Loading)
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()

    private val _selectedPeriod = MutableStateFlow("all")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    private val _selectedCategories = MutableStateFlow<List<String>>(emptyList())
    val selectedCategories: StateFlow<List<String>> = _selectedCategories.asStateFlow()

    private val _totalAllTime = MutableStateFlow(0.0)
    val totalAllTime: StateFlow<Double> = _totalAllTime.asStateFlow()

    private var currentCarId: Int? = null

    init {
        loadCarIdAndData()
    }

    private fun loadCarIdAndData() {
        viewModelScope.launch {
            try {
                // Получаем car_id по номеру автомобиля
                val carResponse = authApi.getCarByPlate(numberPlate)
                if (carResponse.isSuccessful) {
                    currentCarId = carResponse.body()?.carId
                    currentCarId?.let { carId ->
                        loadAllTimeTotal(carId)
                        loadExpenses(carId)
                    }
                } else {
                    _uiState.value = ExpensesUiState.Error("Не удалось получить данные автомобиля")
                }
            } catch (e: Exception) {
                _uiState.value = ExpensesUiState.Error("Ошибка сети: ${e.localizedMessage}")
                Log.e("ExpensesVM", "Ошибка загрузки car_id", e)
            }
        }
    }

    private suspend fun loadAllTimeTotal(carId: Int) {
        try {
            val response = authApi.getExpenses(carId = carId, period = "all")
            if (response.isSuccessful) {
                response.body()?.let { data ->
                    _totalAllTime.value = data.total_spent
                }
            }
        } catch (e: Exception) {
            Log.e("ExpensesVM", "Ошибка загрузки общей суммы", e)
        }
    }

    fun loadExpenses(carId: Int? = null) {
        val id = carId ?: currentCarId ?: return

        viewModelScope.launch {
            _uiState.value = ExpensesUiState.Loading

            try {
                val response = authApi.getExpenses(
                    carId = id,
                    period = _selectedPeriod.value,
                    category = _selectedCategories.value
                )

                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        _uiState.value = ExpensesUiState.Success(
                            totalSpent = data.total_spent,
                            expenses = data.expenses
                        )
                    }
                } else {
                    _uiState.value = ExpensesUiState.Error("Ошибка сервера: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = ExpensesUiState.Error("Сетевая ошибка: ${e.localizedMessage}")
                Log.e("ExpensesVM", "Ошибка загрузки расходов", e)
            }
        }
    }

    fun changePeriod(period: String) {
        _selectedPeriod.value = period
        loadExpenses()
    }

    fun changeCategories(categories: List<String>) {
        _selectedCategories.value = categories
        loadExpenses()
    }

    fun addExpense(category: String, amount: Double, date: String) {
        val carId = currentCarId ?: return

        viewModelScope.launch {
            try {
                val request = AddExpenseRequest(
                    category = category,
                    amount = amount,
                    date = date
                )
                val response = authApi.addExpense(carId, request)

                if (response.isSuccessful) {
                    loadExpenses()           // обновляем список
                    loadAllTimeTotal(carId)  // обновляем общую сумму
                }
            } catch (e: Exception) {
                Log.e("ExpensesVM", "Ошибка добавления расхода", e)
            }
        }
    }
}

// ==================== Состояния ====================

sealed interface ExpensesUiState {
    data object Loading : ExpensesUiState
    data class Success(
        val totalSpent: Double,
        val expenses: List<ExpenseItem>
    ) : ExpensesUiState

    data class Error(val message: String) : ExpensesUiState
}