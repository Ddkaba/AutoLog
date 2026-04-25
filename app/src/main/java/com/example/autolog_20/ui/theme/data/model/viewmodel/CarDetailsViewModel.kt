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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autolog_20.R
import com.example.autolog_20.ui.theme.data.api.AuthApi
import com.example.autolog_20.ui.theme.data.locale.TokenManager
import com.example.autolog_20.ui.theme.data.model.CarDetailsUiState
import com.example.autolog_20.ui.theme.data.model.response.CarDetailResponse
import com.example.autolog_20.ui.theme.data.model.response.TireResponse
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
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

    private val _showTireDialog = MutableStateFlow(TokenManager.shouldShowTireDialog())
    val showTireDialog: StateFlow<Boolean> = _showTireDialog.asStateFlow()

    private val _tireRecommendation = MutableStateFlow<TireResponse?>(null)
    val tireRecommendation: StateFlow<TireResponse?> = _tireRecommendation.asStateFlow()

    init {
        loadCarDetails()
        if (!TokenManager.shouldShowTireDialog()) {
            checkTireRecommendation()
        } else {
            Log.d("CarDetailsVM", "Тип резины не выбран → показываем диалог")
        }
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
                Log.e("CarDetailsVM", "Ошибка загрузки деталей авто", e)
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
                        // Рекомендаций нет - показываем пустой список или сообщение
                        _uiState.value = CarDetailsUiState.Success(emptyList())
                        Log.d("CarDetailsVM", "Нет рекомендаций для автомобиля ID: $carId")
                    } else {
                        _uiState.value = CarDetailsUiState.Success(recs)
                    }
                } else {
                    _uiState.value = CarDetailsUiState.Error("Ошибка рекомендаций: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = CarDetailsUiState.Error("Сетевая ошибка: ${e.localizedMessage}")
                Log.e("CarDetailsVM", "Ошибка загрузки рекомендаций", e)
            }
        }
    }

    // Метод generateAndAddRecommendations полностью удален

    fun setTireType(tireType: String) {
        TokenManager.setCurrentTires(tireType)
        _showTireDialog.value = false
        Log.d("CarDetailsVM", "Выбран тип резины: $tireType → запускаем проверку")
        checkTireRecommendation()
    }

    fun clearTireRecommendation() {
        _tireRecommendation.value = null
        Log.d("CarDetailsVM", "Всесезонная резина выбрана → рекомендация очищена")
    }

    fun checkTireRecommendation() {
        viewModelScope.launch {
            val currentTires = TokenManager.getCurrentTires() ?: run {
                Log.w("CarDetailsVM", "Тип резины не установлен → пропускаем запрос")
                return@launch
            }

            val location = try {
                getCurrentLocation()
            } catch (e: Exception) {
                Log.e("CarDetailsVM", "Ошибка получения геолокации", e)
                null
            }

            if (location == null) {
                Log.w("CarDetailsVM", "Геолокация недоступна")
                return@launch
            }

            try {
                Log.d(
                    "CarDetailsVM",
                    "Запрос по резине: lat=${location.latitude}, lon=${location.longitude}, tires=$currentTires"
                )

                val response = authApi.getTireRecommendation(
                    lat = location.latitude,
                    lon = location.longitude,
                    currentTires = currentTires
                )

                if (response.isSuccessful) {
                    val tire = response.body()
                    _tireRecommendation.value = tire
                    if (tire != null) {
                        Log.i("CarDetailsVM", "Рекомендация по резине: ${tire.recommendation}")
                        if (tire.shouldChangeTo != currentTires) {
                            sendTireNotification(tire)
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.w("CarDetailsVM", "Ошибка сервера по резине: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                Log.e("CarDetailsVM", "Ошибка запроса рекомендации по резине", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { cont ->
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    Log.d(
                        "CarDetailsVM",
                        "Получена локация: ${location.latitude}, ${location.longitude}"
                    )
                    cont.resume(location)
                } else {
                    Log.w("CarDetailsVM", "Последняя локация null")
                    cont.resume(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("CarDetailsVM", "Не удалось получить локацию", e)
                cont.resumeWithException(e)
            }
    }

    private fun sendTireNotification(tire: TireResponse) {
        Log.d("CarDetailsVM", "Попытка отправить уведомление о смене резины")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("CarDetailsVM", "Разрешение POST_NOTIFICATIONS не предоставлено → уведомление не отправлено")
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
            Log.i("CarDetailsVM", "Уведомление успешно отправлено (id: $notificationId)")
        } catch (e: SecurityException) {
            Log.e("CarDetailsVM", "SecurityException при отправке уведомления", e)
        } catch (e: Exception) {
            Log.e("CarDetailsVM", "Ошибка отправки уведомления", e)
        }
    }
}