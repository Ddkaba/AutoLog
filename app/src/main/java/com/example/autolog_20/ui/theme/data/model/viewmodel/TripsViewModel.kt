package com.example.autolog_20.ui.theme.data.model.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autolog_20.ui.theme.data.api.RetrofitClient
import com.example.autolog_20.ui.theme.data.model.request.AddMileageRequest
import com.example.autolog_20.ui.theme.data.tracking.Trip
import com.example.autolog_20.ui.theme.data.tracking.TripDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TripsViewModel(
    private val context: Context
) : ViewModel() {

    private val db = TripDatabase.getDatabase(context)
    private val authApi = RetrofitClient.api

    private val _unassignedTrips = MutableStateFlow<List<Trip>>(emptyList())
    val unassignedTrips: StateFlow<List<Trip>> = _unassignedTrips

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadUnassignedTrips() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _unassignedTrips.value = db.tripDao().getUnassignedTrips()
                android.util.Log.d("TripsViewModel", "Загружено ${_unassignedTrips.value.size} непривязанных поездок")
            } catch (e: Exception) {
                android.util.Log.e("TripsViewModel", "Ошибка загрузки: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Добавление пробега и удаление поездки
    fun addMileageAndDeleteTrip(
        numberPlate: String,
        tripId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                android.util.Log.d("TripsViewModel", "🚗 Поиск авто по номеру: $numberPlate")

                // 1. Получаем ID автомобиля по номеру
                val carResponse = authApi.getCarByPlate(numberPlate)
                if (!carResponse.isSuccessful || carResponse.body() == null) {
                    android.util.Log.e("TripsViewModel", "Автомобиль с номером $numberPlate не найден")
                    onError("Автомобиль с номером $numberPlate не найден")
                    return@launch
                }

                val carId = carResponse.body()!!.carId
                android.util.Log.d("TripsViewModel", "✅ Найден авто: ID=$carId, ${carResponse.body()!!.brand} ${carResponse.body()!!.model}")

                // 2. Получаем поездку из БД
                val trip = db.tripDao().getTripById(tripId)
                if (trip == null) {
                    android.util.Log.e("TripsViewModel", "Поездка не найдена")
                    onError("Поездка не найдена")
                    return@launch
                }

                // 3. Получаем текущий пробег с сервера
                val mileageResponse = authApi.getMileage(carId, "all")
                if (!mileageResponse.isSuccessful || mileageResponse.body() == null) {
                    android.util.Log.e("TripsViewModel", "Ошибка получения пробега: ${mileageResponse.code()}")
                    onError("Ошибка получения пробега")
                    return@launch
                }

                val logs = mileageResponse.body()!!.logs
                val lastLog = logs.maxByOrNull { it.logId } ?: logs.lastOrNull()
                val currentMileageKm = lastLog?.mileage?.toDouble() ?: 0.0
                val tripDistanceKm = trip.distance / 1000.0
                val newTotalMileageKm = currentMileageKm + tripDistanceKm

                android.util.Log.d("TripsViewModel", """
                    📊 Расчет пробега:
                    Авто: ${carResponse.body()!!.brand} ${carResponse.body()!!.model}
                    Последний пробег: $currentMileageKm км
                    Расстояние поездки: ${String.format("%.2f", tripDistanceKm)} км
                    Новый пробег: ${String.format("%.2f", newTotalMileageKm)} км
                """.trimIndent())

                // 4. Создаем новую запись пробега на сервере
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val currentDate = dateFormat.format(Date())

                val createRequest = AddMileageRequest(
                    date = currentDate,
                    mileage = newTotalMileageKm.toInt(),
                    route = "Автоматическая запись: ${String.format("%.2f", tripDistanceKm)} км"
                )

                val createResponse = authApi.addMileageRecord(carId, createRequest)
                if (!createResponse.isSuccessful) {
                    android.util.Log.e("TripsViewModel", "Ошибка создания записи: ${createResponse.code()}")
                    onError("Ошибка создания записи пробега")
                    return@launch
                }

                android.util.Log.d("TripsViewModel", "✅ Создана запись пробега: ${newTotalMileageKm.toInt()} км")

                // 5. Удаляем поездку из локальной БД
                db.tripDao().deleteTrip(trip)
                db.gpsPointDao().deletePointsByTrip(trip.id)

                android.util.Log.d("TripsViewModel", "✅ Поездка ${trip.id} удалена из локальной БД")

                // 6. Обновляем список
                loadUnassignedTrips()

                onSuccess()

            } catch (e: Exception) {
                android.util.Log.e("TripsViewModel", "Ошибка: ${e.message}")
                e.printStackTrace()
                onError(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    private fun formatDate(date: Date): String {
        val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return format.format(date)
    }
}