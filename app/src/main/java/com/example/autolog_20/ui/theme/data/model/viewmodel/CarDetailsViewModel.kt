package com.example.autolog_20.ui.theme.data.model.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autolog_20.R
import com.example.autolog_20.ui.theme.data.api.AuthApi
import com.example.autolog_20.ui.theme.data.locale.TokenManager
import com.example.autolog_20.ui.theme.data.model.CarDetailsUiState
import com.example.autolog_20.ui.theme.data.model.ExpensesData
import com.example.autolog_20.ui.theme.data.model.request.CarUpdateRequest
import com.example.autolog_20.ui.theme.data.model.response.CarDetailResponse
import com.example.autolog_20.ui.theme.data.model.response.TireResponse
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CarDetailsViewModel(
    private val authApi: AuthApi,
    private val numberPlate: String,
    private val context: Context
) : ViewModel() {

    private val _carDetail = MutableStateFlow<CarDetailResponse?>(null)
    val carDetail: StateFlow<CarDetailResponse?> = _carDetail.asStateFlow()

    private val _uiState = MutableStateFlow<CarDetailsUiState>(CarDetailsUiState.Loading)
    val uiState: StateFlow<CarDetailsUiState> = _uiState.asStateFlow()

    private val _showTireDialog = MutableStateFlow(TokenManager.shouldShowTireDialog(numberPlate))
    val showTireDialog: StateFlow<Boolean> = _showTireDialog.asStateFlow()

    private val _tireRecommendation = MutableStateFlow<TireResponse?>(null)
    val tireRecommendation: StateFlow<TireResponse?> = _tireRecommendation.asStateFlow()

    private val _monthlyExpenses = MutableStateFlow<ExpensesData?>(null)
    val monthlyExpenses: StateFlow<ExpensesData?> = _monthlyExpenses.asStateFlow()

    private val _isLoadingExpenses = MutableStateFlow(false)
    val isLoadingExpenses: StateFlow<Boolean> = _isLoadingExpenses.asStateFlow()

    private val _nextServiceDistance = MutableStateFlow<Int?>(null)
    val nextServiceDistance: StateFlow<Int?> = _nextServiceDistance.asStateFlow()

    private val _currentMileage = MutableStateFlow<Int?>(null)
    val currentMileage: StateFlow<Int?> = _currentMileage.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private var isCheckingRecommendation = false

    init {
        loadCarDetails()
        _showTireDialog.value = TokenManager.shouldShowTireDialog(numberPlate)
    }

    private fun loadCarDetails() {
        viewModelScope.launch {
            try {
                val response = authApi.getCarByPlate(numberPlate)
                if (response.isSuccessful) {
                    val car = response.body()
                    if (car != null) {
                        _carDetail.value = car
                        loadRecommendations(car.carId)
                    } else {
                        _uiState.value = CarDetailsUiState.Error("Машина не найдена")
                    }
                } else {
                    _uiState.value = CarDetailsUiState.Error("Ошибка загрузки: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = CarDetailsUiState.Error("Сетевая ошибка: ${e.localizedMessage}")
                Timber.tag("CarDetailsVM").e(e, "Ошибка загрузки деталей авто")
            }
        }
    }

    private fun loadRecommendations(carId: Int) {
        viewModelScope.launch {
            try {
                val response = authApi.getRecommendations(carId)
                if (response.isSuccessful) {
                    val recs = response.body() ?: emptyList()
                    if (recs.isEmpty()) {
                        _uiState.value = CarDetailsUiState.Success(emptyList())
                        Timber.tag("CarDetailsVM").d("Нет рекомендаций для автомобиля ID: $carId")
                    } else {
                        _uiState.value = CarDetailsUiState.Success(recs)
                    }
                } else {
                    _uiState.value = CarDetailsUiState.Error("Ошибка рекомендаций: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = CarDetailsUiState.Error("Сетевая ошибка: ${e.localizedMessage}")
                Timber.tag("CarDetailsVM").e(e, "Ошибка загрузки рекомендаций")
            }
        }
    }

    fun loadMonthlyExpenses(carId: Int) {
        viewModelScope.launch {
            _isLoadingExpenses.value = true
            try {
                val response = authApi.getExpenses(
                    carId = carId,
                    period = "month",
                    from = null,
                    to = null,
                    category = emptyList()
                )

                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        _monthlyExpenses.value = ExpensesData(
                            totalSpent = data.totalSpent,
                            categories = data.expenses.groupBy { it.category?.name ?: "Прочие" }
                                .mapValues {
                                    it.value.sumOf { e ->
                                        e.amount.toDoubleOrNull() ?: 0.0
                                    }
                                }
                                .keys.take(3)
                        )
                    }
                } else {
                    Timber.tag("CarDetailsVM").e("Ошибка загрузки расходов: ${response.code()}")
                }
            } catch (e: Exception) {
                Timber.tag("CarDetailsVM").e(e, "Ошибка загрузки расходов")
            } finally {
                _isLoadingExpenses.value = false
            }
        }
    }

    fun setTireType(tireType: String) {
        TokenManager.setCurrentTires(numberPlate, tireType)
        _showTireDialog.value = false
        Timber.tag("CarDetailsVM").d("Выбран тип резины: $tireType -> запускаем проверку")
        checkTireRecommendation()
    }

    fun checkTireRecommendation() {
        Timber.tag("CarDetailsVM").d("checkTireRecommendation() called")

        if (isCheckingRecommendation) {
            Timber.tag("CarDetailsVM").d("Проверка резины уже выполняется, пропускаем")
            return
        }

        val hasLocationPermission = checkLocationPermission()
        Timber.tag("CarDetailsVM").d("hasLocationPermission: $hasLocationPermission")

        if (!hasLocationPermission) {
            Timber.tag("CarDetailsVM").d("Нет разрешения на геолокацию, пропускаем")
            return
        }

        val currentTires = TokenManager.getCurrentTires(numberPlate)
        Timber.tag("CarDetailsVM").d("currentTires: $currentTires")

        if (currentTires.isNullOrBlank()) {
            Timber.tag("CarDetailsVM").w("Тип резины не установлен -> пропускаем запрос")
            return
        }

        viewModelScope.launch {
            isCheckingRecommendation = true
            try {
                Timber.tag("CarDetailsVM").d("Пытаемся получить геолокацию...")
                val location = try {
                    getCurrentLocation()
                } catch (e: Exception) {
                    Timber.tag("CarDetailsVM").e(e, "Ошибка получения геолокации")
                    null
                }

                if (location == null) {
                    Timber.tag("CarDetailsVM").w("Геолокация недоступна")
                    return@launch
                }

                Timber.tag("CarDetailsVM")
                    .d("Отправляем запрос: lat=${location.latitude}, lon=${location.longitude}, tires=$currentTires")

                val response = authApi.getTireRecommendation(
                    lat = location.latitude,
                    lon = location.longitude,
                    currentTires = currentTires
                )

                if (response.isSuccessful) {
                    val tire = response.body()
                    _tireRecommendation.value = tire
                    if (tire != null) {
                        Timber.tag("CarDetailsVM")
                            .i("Рекомендация по резине: ${tire.recommendation}")
                        if (tire.shouldChangeTo != currentTires) {
                            sendTireNotification(tire)
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Timber.tag("CarDetailsVM")
                        .w("Ошибка сервера: ${response.code()} - $errorBody")
                }
            } finally {
                isCheckingRecommendation = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { cont ->
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            null
        )
            .addOnSuccessListener { location ->
                cont.resume(location)
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }
    }

    private fun checkLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun sendTireNotification(tire: TireResponse) {
        Timber.tag("CarDetailsVM").d("Попытка отправить уведомление о смене резины")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Timber.tag("CarDetailsVM")
                .w("Разрешение POST_NOTIFICATIONS не предоставлено → уведомление не отправлено")
            return
        }

        val channelId = "autolog_tires_channel"
        val notificationId = tire.hashCode()

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(tire.recommendation)
            .setContentText(tire.reason)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val notificationManager = NotificationManagerCompat.from(context)

        try {
            notificationManager.notify(notificationId, builder.build())
            Timber.tag("CarDetailsVM").i("Уведомление успешно отправлено (id: $notificationId)")
        } catch (e: SecurityException) {
            Timber.tag("CarDetailsVM").e(e, "SecurityException при отправке уведомления")
        } catch (e: Exception) {
            Timber.tag("CarDetailsVM").e(e, "Ошибка отправки уведомления")
        }
    }

    fun updateCarDetails(
        color: String,
        numberPlate: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val currentCar = _carDetail.value ?: run {
                    onError("Данные автомобиля не загружены")
                    return@launch
                }

                val request = CarUpdateRequest(
                    color = color.trim(),
                    numberPlate = numberPlate.trim().uppercase()
                )

                val response = authApi.updateCar(currentCar.carId, request)

                if (response.isSuccessful) {
                    val updatedCar = response.body()
                    if (updatedCar != null) {
                        _carDetail.value = updatedCar
                    }
                    onSuccess()
                    Timber.tag("CarDetailsVM").d("Автомобиль успешно обновлен")
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Некорректные данные"
                        401 -> "Не авторизован"
                        403 -> "Нет прав на редактирование"
                        404 -> "Автомобиль не найден"
                        else -> "Ошибка обновления: ${response.code()}"
                    }
                    onError(errorMessage)
                    Timber.tag("CarDetailsVM").e("Ошибка обновления: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Сетевая ошибка: ${e.localizedMessage}")
                Timber.tag("CarDetailsVM").e(e, "Ошибка обновления автомобиля")
            }
        }
    }

    fun loadCurrentMileageAndCalculateNextService(carId: Int) {
        viewModelScope.launch {
            try {
                val mileageResponse = authApi.getMileage(carId, "all")
                if (mileageResponse.isSuccessful) {
                    val data = mileageResponse.body()
                    val lastLog = data?.logs?.maxByOrNull { it.date }
                    val currentMileageValue = lastLog?.mileage
                    _currentMileage.value = currentMileageValue

                    if (currentMileageValue != null) {
                        val recommendationsResponse = authApi.getRecommendations(carId)
                        if (recommendationsResponse.isSuccessful) {
                            val recommendations = recommendationsResponse.body() ?: emptyList()

                            val nextService = recommendations
                                .filter { it.nextRecommendedMileage != null && it.nextRecommendedMileage!! > currentMileageValue }
                                .minByOrNull { it.nextRecommendedMileage ?: Int.MAX_VALUE }

                            if (nextService != null && nextService.nextRecommendedMileage != null) {
                                val distance = nextService.nextRecommendedMileage!! - currentMileageValue
                                _nextServiceDistance.value = distance
                            } else {
                                _nextServiceDistance.value = null
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Ошибка загрузки пробега и расчета ТО")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation() {
        viewModelScope.launch {
            val location = getCurrentLocation()
            _currentLocation.value = location
        }
    }
}