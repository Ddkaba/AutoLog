package com.example.autolog_20.ui.theme.data.tracking

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.autolog_20.ui.theme.data.api.RetrofitClient
import com.example.autolog_20.ui.theme.data.model.Trip
import com.example.autolog_20.ui.theme.data.model.request.AddMileageRequest
import com.example.autolog_20.ui.theme.data.room_database.TripDatabase
import java.text.SimpleDateFormat
import java.util.*

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val authApi = RetrofitClient.api

    override suspend fun doWork(): Result {
        return try {
            val trips = TripDatabase.getDatabase(applicationContext)
                .tripDao()
                .getUnsyncedTrips()

            for (trip in trips) {
                if (trip.carId != null) {
                    syncTripToServer(trip)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun syncTripToServer(trip: Trip) {
        try {
            // Форматируем дату правильно
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateString = dateFormat.format(trip.startTime)

            val request = AddMileageRequest(
                date = dateString,
                mileage = (trip.distance / 1000).toInt(), // Конвертируем метры в километры
                route = "Поездка ${dateString}"
            )

            val response = authApi.addMileageRecord(trip.carId!!, request)

            if (response.isSuccessful) {
                TripDatabase.getDatabase(applicationContext)
                    .tripDao()
                    .markAsSynced(trip.id)
            }
        } catch (e: Exception) {
            // Логируем ошибку
            e.printStackTrace()
        }
    }
}