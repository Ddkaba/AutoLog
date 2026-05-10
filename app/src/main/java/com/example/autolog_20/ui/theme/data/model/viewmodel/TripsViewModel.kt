package com.example.autolog_20.ui.theme.data.model.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autolog_20.ui.theme.data.api.RetrofitClient
import com.example.autolog_20.ui.theme.data.model.request.AddMileageRequest
import com.example.autolog_20.ui.theme.data.tracking.Trip
import com.example.autolog_20.ui.theme.data.tracking.TripDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
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
            } catch (e: Exception) {
                Timber.tag("TripsViewModel").e("Ошибка загрузки: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addMileageAndDeleteTrip(
        numberPlate: String,
        tripId: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val carResponse = authApi.getCarByPlate(numberPlate)
                if (!carResponse.isSuccessful || carResponse.body() == null) {
                    onError("Автомобиль с номером $numberPlate не найден")
                    return@launch
                }

                val carId = carResponse.body()!!.carId

                val trip = db.tripDao().getTripById(tripId)
                if (trip == null) {
                    onError("Поездка не найдена")
                    return@launch
                }

                val mileageResponse = authApi.getMileage(carId, "all")
                if (!mileageResponse.isSuccessful || mileageResponse.body() == null) {
                    onError("Ошибка получения пробега")
                    return@launch
                }

                val logs = mileageResponse.body()!!.logs
                val lastLog = logs.maxByOrNull { it.logId } ?: logs.lastOrNull()
                val currentMileageKm = lastLog?.mileage?.toDouble() ?: 0.0
                val tripDistanceKm = trip.distance / 1000.0
                val newTotalMileageKm = currentMileageKm + tripDistanceKm

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val currentDate = dateFormat.format(Date())

                val createRequest = AddMileageRequest(
                    date = currentDate,
                    mileage = newTotalMileageKm.toInt(),
                    route = "Автоматическая запись: ${String.format("%.2f", tripDistanceKm)} км"
                )

                val createResponse = authApi.addMileageRecord(carId, createRequest)
                if (!createResponse.isSuccessful) {
                    onError("Ошибка создания записи пробега")
                    return@launch
                }

                db.tripDao().deleteTrip(trip)
                db.gpsPointDao().deletePointsByTrip(trip.id)

                loadUnassignedTrips()

                onSuccess()

            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Неизвестная ошибка")
            }
        }
    }
}