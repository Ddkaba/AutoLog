package com.example.autolog_20.ui.theme.data.model.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autolog_20.ui.theme.data.api.AuthApi
import com.example.autolog_20.ui.theme.data.model.ExpenseItem
import com.example.autolog_20.ui.theme.data.model.ExpensesUiState
import com.example.autolog_20.ui.theme.data.model.request.ExpenseUpdateRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
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

    private val _customFrom = MutableStateFlow<String?>(null)
    val customFrom: StateFlow<String?> = _customFrom.asStateFlow()

    private val _customTo = MutableStateFlow<String?>(null)
    val customTo: StateFlow<String?> = _customTo.asStateFlow()

    private var currentCarId: Int? = null

    init {
        loadCarIdAndData()
    }

    private fun loadCarIdAndData() {
        viewModelScope.launch {
            try {
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
                Timber.tag("ExpensesVM").e(e, "Ошибка загрузки car_id")
            }
        }
    }

    private suspend fun loadAllTimeTotal(carId: Int) {
        try {
            val response = authApi.getExpenses(carId = carId, period = "all")
            if (response.isSuccessful) {
                response.body()?.let { data ->
                    _totalAllTime.value = data.totalSpent
                }
            }
        } catch (e: Exception) {
            Timber.tag("ExpensesVM").e(e, "Ошибка загрузки общей суммы")
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
                    from = if (period == "custom") _customFrom.value else null,
                    to = if (period == "custom") _customTo.value else null,
                    category = _selectedCategories.value
                )

                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        _uiState.value = ExpensesUiState.Success(
                            totalSpent = data.totalSpent,
                            expenses = data.expenses
                        )
                    }
                } else {
                    _uiState.value = ExpensesUiState.Error("Ошибка сервера: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = ExpensesUiState.Error("Сетевая ошибка: ${e.localizedMessage}")
                Timber.tag("ExpensesVM").e(e, "Ошибка загрузки расходов")
            }
        }
    }

    fun changePeriod(period: String) {
        _selectedPeriod.value = period
        if (period != "custom") {
            _customFrom.value = null
            _customTo.value = null
        }
        loadExpenses()
    }

    fun setCustomPeriod(from: LocalDate, to: LocalDate) {
        _selectedPeriod.value = "custom"
        _customFrom.value = from.toString()
        _customTo.value = to.toString()
        loadExpenses()
    }

    fun changeCategories(categories: List<String>) {
        _selectedCategories.value = categories
        loadExpenses()
    }

    fun deleteExpense(expense: ExpenseItem, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val carId = currentCarId ?: run {
            onError("ID автомобиля не найден")
            return
        }

        viewModelScope.launch {
            try {
                val response = authApi.deleteExpense(carId, expense.expenseId)

                if (response.isSuccessful) {
                    loadExpenses()
                    loadAllTimeTotal(carId)
                    onSuccess()
                    Timber.tag("ExpensesVM").d("Расход успешно удален: ${expense.expenseId}")
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Не авторизован"
                        403 -> "Нет прав на удаление"
                        404 -> "Расход не найден"
                        else -> "Ошибка удаления: ${response.code()}"
                    }
                    onError(errorMessage)
                    Timber.tag("ExpensesVM").e("Ошибка удаления: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Сетевая ошибка: ${e.localizedMessage}")
                Timber.tag("ExpensesVM").e(e, "Ошибка удаления расхода")
            }
        }
    }

    fun updateExpense(
        expense: ExpenseItem,
        amount: Double? = null,
        date: String? = null,
        description: String? = null,
        categoryId: Int? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val carId = currentCarId ?: run {
            onError("ID автомобиля не найден")
            return
        }

        val hasChanges = (amount != null && amount != expense.amount.toDoubleOrNull()) ||
                (date != null && date != expense.date) ||
                (description != null && description != expense.description) ||
                (categoryId != null && categoryId != expense.category?.categoryId)

        if (!hasChanges) {
            onError("Нет изменений для сохранения")
            return
        }

        viewModelScope.launch {
            try {
                val request = ExpenseUpdateRequest(
                    amount = amount,
                    date = date,
                    description = description,
                    categoryId = categoryId
                )

                val response = authApi.updateExpense(carId, expense.expenseId, request)

                if (response.isSuccessful) {
                    loadExpenses()
                    loadAllTimeTotal(carId)
                    onSuccess()
                    Timber.tag("ExpensesVM").d("Расход успешно обновлен: ${expense.expenseId}")
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Некорректные данные"
                        401 -> "Не авторизован"
                        403 -> "Нет прав на редактирование"
                        404 -> "Расход не найден"
                        else -> "Ошибка обновления: ${response.code()}"
                    }
                    onError(errorMessage)
                    Timber.tag("ExpensesVM").e("Ошибка обновления: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Сетевая ошибка: ${e.localizedMessage}")
                Timber.tag("ExpensesVM").e(e, "Ошибка обновления расхода")
            }
        }
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
                    categoryId = categoryIdBody,
                    amount = amountBody,
                    date = dateBody,
                    description = descBody,
                    receipt_photo = receiptPart
                )

                if (response.isSuccessful) {
                    loadExpenses()
                    loadAllTimeTotal(carId)
                } else {
                    Timber.tag("ExpensesVM").e("Ошибка: ${response.code()}")
                }
            } catch (e: Exception) {
                Timber.tag("ExpensesVM").e(e, "Ошибка отправки")
            }
        }
    }
}