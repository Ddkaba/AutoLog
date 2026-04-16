package com.example.autolog_20.ui.theme.data.model

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autolog_20.ui.theme.data.api.AuthApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate

class ExpensesViewModel(
    private val authApi: AuthApi,
    private val numberPlate: String,
    private val context: Context
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
    var customFrom: String? = null
    var customTo: String? = null

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
                val period = _selectedPeriod.value

                val response = authApi.getExpenses(
                    carId = id,
                    period = period,
                    from = if (period == "custom") customFrom else null,
                    to = if (period == "custom") customTo else null,
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
        if (period != "custom") {
            customFrom = null
            customTo = null
        }
        loadExpenses()
    }

    fun changeCategories(categories: List<String>) {
        _selectedCategories.value = categories
        loadExpenses()
    }

    fun setCustomPeriod(from: LocalDate, to: LocalDate) {
        _selectedPeriod.value = "custom"
        customFrom = from.toString()
        customTo = to.toString()
        loadExpenses()
    }

    fun addExpenseWithReceipt(
        category: String,
        categoryId: Int,
        amount: Double,
        date: String,
        description: String?,
        photoUri: Uri?
    ) {
        val carId = currentCarId ?: return

        viewModelScope.launch {
            try {
                val categoryBody = category.toRequestBody("text/plain".toMediaType())
                val categoryIdBody = categoryId.toString().toRequestBody("text/plain".toMediaType())
                val amountBody = amount.toString().toRequestBody("text/plain".toMediaType())
                val dateBody = date.toRequestBody("text/plain".toMediaType())
                val descBody = description?.toRequestBody("text/plain".toMediaType())

                val receiptPart = photoUri?.let { uri ->
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes() ?: ByteArray(0)
                    inputStream?.close()

                    //val requestBody = bytes.toRequestBody("image/jpeg".toMediaType())
                    val mimeType = context.contentResolver.getType(uri) ?: "image/*"
                    val requestBody = bytes.toRequestBody(mimeType.toMediaType())
                    MultipartBody.Part.createFormData(
                        "receipt_photo",
                        "receipt_${System.currentTimeMillis()}.jpg",
                        requestBody
                    )
                }

                val response = authApi.addExpenseWithReceipt(
                    carId = carId,
                    category = categoryBody,
                    categoryId = categoryIdBody,      // ← передаём category_id
                    amount = amountBody,
                    date = dateBody,
                    description = descBody,
                    receipt_photo = receiptPart
                )

                if (response.isSuccessful) {
                    Log.d("ExpensesVM", "Расход успешно добавлен с чеком и категорией")
                    loadExpenses()
                    loadAllTimeTotal(carId)
                } else {
                    Log.e("ExpensesVM", "Ошибка: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ExpensesVM", "Ошибка отправки", e)
            }
        }
    }
}


sealed interface ExpensesUiState {
    data object Loading : ExpensesUiState
    data class Success(
        val totalSpent: Double,
        val expenses: List<ExpenseItem>
    ) : ExpensesUiState

    data class Error(val message: String) : ExpensesUiState
}