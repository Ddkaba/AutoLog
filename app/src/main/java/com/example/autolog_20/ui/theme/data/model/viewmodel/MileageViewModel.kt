package com.example.autolog_20.ui.theme.data.model.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autolog_20.ui.theme.data.api.AuthApi
import com.example.autolog_20.ui.theme.data.model.MileageLog
import com.example.autolog_20.ui.theme.data.model.MileageUiState
import com.example.autolog_20.ui.theme.data.model.request.AddMileageRequest
import com.example.autolog_20.ui.theme.data.model.request.UpdateMileageRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.sortedByDescending

class MileageViewModel(
    private val authApi: AuthApi,
    private val numberPlate: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(MileageUiState(isLoading = true))
    val uiState: StateFlow<MileageUiState> = _uiState.asStateFlow()

    private var currentCarId: Int? = null

    init {
        loadCarIdAndMileage()
    }

    private fun loadCarIdAndMileage() {
        viewModelScope.launch {
            try {
                val carResponse = authApi.getCarByPlate(numberPlate)
                if (carResponse.isSuccessful) {
                    currentCarId = carResponse.body()?.carId
                    if (currentCarId != null) {
                        loadMileage()
                    } else {
                        _uiState.value = MileageUiState(
                            isLoading = false,
                            error = "Автомобиль не найден"
                        )
                    }
                } else {
                    _uiState.value = MileageUiState(
                        isLoading = false,
                        error = "Ошибка загрузки: ${carResponse.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = MileageUiState(
                    isLoading = false,
                    error = "Сетевая ошибка: ${e.localizedMessage}"
                )
            }
        }
    }

    fun loadMileage(period: String = _uiState.value.selectedPeriod) {
        val carId = currentCarId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val response = authApi.getMileage(carId, period)
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        val logs =
                            data.logs.sortedWith(compareBy<MileageLog> { it.date }.thenBy { it.logId })
                        val currentMileage = logs.lastOrNull()?.mileage

                        _uiState.value = MileageUiState(
                            isLoading = false,
                            logs = logs,
                            currentMileage = currentMileage,
                            selectedPeriod = period
                        )
                    } else {
                        _uiState.value = MileageUiState(
                            isLoading = false,
                            logs = emptyList(),
                            selectedPeriod = period,
                            error = "Нет данных о пробеге"
                        )
                    }
                } else {
                    _uiState.value = MileageUiState(
                        isLoading = false,
                        error = "Ошибка загрузки: ${response.code()}",
                        selectedPeriod = period
                    )
                }
            } catch (e: Exception) {
                _uiState.value = MileageUiState(
                    isLoading = false,
                    error = "Сетевая ошибка: ${e.localizedMessage}",
                    selectedPeriod = period
                )
            }
        }
    }

    fun addMileageRecord(
        date: String,
        mileage: Int,
        route: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val carId = currentCarId ?: run {
            onError("ID автомобиля не найден")
            return
        }

        viewModelScope.launch {
            try {
                val response = authApi.addMileageRecord(
                    carId = carId,
                    request = AddMileageRequest(
                        date = date,
                        mileage = mileage,
                        route = route
                    )
                )

                if (response.isSuccessful) {
                    loadMileage()
                    onSuccess()
                } else {
                    onError("Ошибка добавления: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Сетевая ошибка: ${e.localizedMessage}")
            }
        }
    }

    fun deleteMileageRecord(
        log: MileageLog,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val carId = currentCarId ?: run {
            onError("ID автомобиля не найден")
            return
        }

        viewModelScope.launch {
            try {
                val response = authApi.deleteMileageRecord(carId, log.logId)
                if (response.isSuccessful) {
                    loadMileage()
                    onSuccess()
                } else {
                    onError("Ошибка удаления: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Сетевая ошибка: ${e.localizedMessage}")
            }
        }
    }

    fun updateMileageRecord(
        log: MileageLog,
        date: String,
        mileage: Int,
        route: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val carId = currentCarId ?: run {
            onError("ID автомобиля не найден")
            return
        }

        viewModelScope.launch {
            try {
                val response = authApi.updateMileageRecord(
                    carId = carId,
                    logId = log.logId,
                    request = UpdateMileageRequest(
                        date = date,
                        mileage = mileage,
                        route = route
                    )
                )
                if (response.isSuccessful) {
                    loadMileage()
                    onSuccess()
                } else {
                    onError("Ошибка обновления: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Сетевая ошибка: ${e.localizedMessage}")
            }
        }
    }


    fun changeCustomPeriod(from: String, to: String) {
        _uiState.value = _uiState.value.copy(
            selectedPeriod = "custom",
            customFrom = from,
            customTo = to
        )
        loadMileageWithCustomPeriod(from, to)
    }

    fun loadMileageWithCustomPeriod(from: String, to: String) {
        val carId = currentCarId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val response = authApi.getMileageWithCustomPeriod(
                    carId = carId,
                    period = "custom",
                    from = from,
                    to = to
                )

                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        val logs =
                            data.logs.sortedWith(compareBy<MileageLog> { it.date }.thenBy { it.logId })
                        val currentMileage = logs.lastOrNull()?.mileage

                        _uiState.value = MileageUiState(
                            isLoading = false,
                            logs = logs,
                            currentMileage = currentMileage,
                            selectedPeriod = "custom",
                            customFrom = from,
                            customTo = to
                        )
                    } else {
                        _uiState.value = MileageUiState(
                            isLoading = false,
                            logs = emptyList(),
                            selectedPeriod = "custom",
                            customFrom = from,
                            customTo = to,
                            error = "Нет данных за выбранный период"
                        )
                    }
                } else {
                    _uiState.value = MileageUiState(
                        isLoading = false,
                        error = "Ошибка загрузки: ${response.code()}",
                        selectedPeriod = "custom",
                        customFrom = from,
                        customTo = to
                    )
                }
            } catch (e: Exception) {
                _uiState.value = MileageUiState(
                    isLoading = false,
                    error = "Сетевая ошибка: ${e.localizedMessage}",
                    selectedPeriod = "custom",
                    customFrom = from,
                    customTo = to
                )
            }
        }
    }

    fun changePeriod(period: String) {
        _uiState.value = _uiState.value.copy(
            selectedPeriod = period,
            customFrom = null,
            customTo = null
        )
        loadMileage(period)
    }
}