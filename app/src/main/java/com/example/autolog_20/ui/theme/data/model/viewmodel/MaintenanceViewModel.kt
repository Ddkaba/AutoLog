package com.example.autolog_20.ui.theme.data.model.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autolog_20.ui.theme.data.api.AuthApi
import com.example.autolog_20.ui.theme.data.model.MaintenanceUiState
import com.example.autolog_20.ui.theme.data.model.ServiceRecord
import com.example.autolog_20.ui.theme.data.model.request.RecommendationUpdateRequest
import com.example.autolog_20.ui.theme.data.model.request.ServiceUpdateRequest
import com.example.autolog_20.ui.theme.data.model.response.RecommendationResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber


class MaintenanceViewModel(
    private val authApi: AuthApi,
    private val numberPlate: String,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<MaintenanceUiState>(MaintenanceUiState.Loading)
    val uiState: StateFlow<MaintenanceUiState> = _uiState.asStateFlow()

    private val _recommendationsForSheet = MutableStateFlow<List<RecommendationResponse>>(emptyList())
    val recommendationsForSheet: StateFlow<List<RecommendationResponse>> = _recommendationsForSheet.asStateFlow()

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
                        loadCompletedServices()
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

    fun loadRecommendationsForSheet() {
        val carId = currentCarId ?: return
        viewModelScope.launch {
            try {
                val response = authApi.getRecommendations(carId)
                if (response.isSuccessful) {
                    val recommendations = response.body() ?: emptyList()
                    _recommendationsForSheet.value = recommendations.sortedBy { it.recommendedMileage }
                }
            } catch (e: Exception) {
                Timber.e(e.toString())
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
        val carId = currentCarId ?: run {
            onError("ID автомобиля не найден")
            return
        }
        viewModelScope.launch {
            try {
                val response = authApi.deleteServiceRecord(carId, service.recordId)

                if (response.isSuccessful) {
                    loadCompletedServices()
                    onSuccess()
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Не авторизован"
                        403 -> "Нет прав на удаление"
                        404 -> "Запись не найдена"
                        else -> "Ошибка удаления: ${response.code()}"
                    }
                    onError(errorMessage)
                }
            } catch (e: Exception) {
                onError("Сетевая ошибка: ${e.localizedMessage}")
            }
        }
    }

    fun updateService(
        service: ServiceRecord,
        date: String? = null,
        mileage: Int? = null,
        cost: Double? = null,
        notes: String? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val carId = currentCarId ?: run {
            onError("ID автомобиля не найден")
            return
        }

        val hasChanges = (cost != null && cost != service.cost.toDoubleOrNull()) ||
                (notes != null && notes != service.notes) ||
                (date != null && date != service.date) ||
                (mileage != null && mileage != service.mileage)

        if (!hasChanges) {
            onError("Нет изменений для сохранения")
            return
        }

        viewModelScope.launch {
            try {
                val request = ServiceUpdateRequest(
                    cost = cost,
                    notes = notes,
                    date = date,
                    mileage = mileage
                )

                val response = authApi.updateServiceRecord(carId, service.recordId, request)

                if (response.isSuccessful) {
                    loadCompletedServices()
                    onSuccess()
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Некорректные данные"
                        401 -> "Не авторизован"
                        403 -> "Нет прав на редактирование"
                        404 -> "Запись не найдена"
                        else -> "Ошибка обновления: ${response.code()}"
                    }
                    onError(errorMessage)
                }
            } catch (e: Exception) {
                onError("Сетевая ошибка: ${e.localizedMessage}")
            }
        }
    }

    fun updateRecommendation(
        recommendation: RecommendationResponse,
        recommendedMileage: Int? = null,
        serviceType: String? = null,
        description: String? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val carId = currentCarId ?: run {
            onError("ID автомобиля не найден")
            return
        }
        val hasChanges = (recommendedMileage != null && recommendedMileage != recommendation.recommendedMileage) ||
                (serviceType != null && serviceType != recommendation.serviceType) ||
                (description != null && description != recommendation.description)

        if (!hasChanges) {
            onError("Нет изменений для сохранения")
            return
        }

        viewModelScope.launch {
            try {
                val request = RecommendationUpdateRequest(
                    recommendedMileage = recommendedMileage,
                    serviceType = serviceType,
                    description = description
                )

                val response = authApi.updateRecommendation(carId, recommendation.recommendationId, request)

                if (response.isSuccessful) {
                    loadPlannedRecommendations()
                    onSuccess()
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Некорректные данные"
                        401 -> "Не авторизован"
                        403 -> "Нет прав на редактирование"
                        404 -> "Рекомендация не найдена"
                        else -> "Ошибка обновления: ${response.code()}"
                    }
                    onError(errorMessage)
                }
            } catch (e: Exception) {
                onError("Сетевая ошибка: ${e.localizedMessage}")
            }
        }
    }

    fun addServiceRecord(
        serviceType: String,
        date: String,
        mileage: Int,
        cost: Double,
        notes: String?,
        recommendationId: Int?,
        photoUri: Uri?,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val carId = currentCarId ?: run {
            onError("ID автомобиля не найден")
            return
        }

        viewModelScope.launch {
            try {
                val serviceTypeBody = serviceType.toRequestBody("text/plain".toMediaType())
                val dateBody = date.toRequestBody("text/plain".toMediaType())
                val mileageBody = mileage.toString().toRequestBody("text/plain".toMediaType())
                val costBody = cost.toString().toRequestBody("text/plain".toMediaType())
                val notesBody = notes?.toRequestBody("text/plain".toMediaType())
                val recommendationIdBody = recommendationId?.toString()?.toRequestBody("text/plain".toMediaType())

                val receiptPart = photoUri?.let { uri ->
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes() ?: ByteArray(0)
                    inputStream?.close()

                    val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                    val requestBody = bytes.toRequestBody(mimeType.toMediaType())
                    MultipartBody.Part.createFormData(
                        "receipt_photo",
                        "receipt_${System.currentTimeMillis()}.jpg",
                        requestBody
                    )
                }

                val response = authApi.createServiceRecord(
                    carId = carId,
                    serviceType = serviceTypeBody,
                    date = dateBody,
                    mileage = mileageBody,
                    cost = costBody,
                    notes = notesBody,
                    recommendationId = recommendationIdBody,
                    receipt_photo = receiptPart
                )

                if (response.isSuccessful) {
                    loadCompletedServices()
                    loadPlannedRecommendations()
                    onSuccess()
                } else {
                    onError("Ошибка добавления: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Сетевая ошибка: ${e.localizedMessage}")
            }
        }
    }
}