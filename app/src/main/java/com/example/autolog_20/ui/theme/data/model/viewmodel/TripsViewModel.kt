package com.example.autolog_20.ui.theme.data.model.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autolog_20.ui.theme.data.locale.SettingsManager
import com.example.autolog_20.ui.theme.data.model.Trip
import com.example.autolog_20.ui.theme.data.room_database.TripDatabase
import com.example.autolog_20.ui.theme.data.tracking.TrackingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class TripsViewModel(
    private val context: Context
) : ViewModel() {

    private val db = TripDatabase.getDatabase(context)

    private val _unassignedTrips = MutableStateFlow<List<Trip>>(emptyList())
    val unassignedTrips: StateFlow<List<Trip>> = _unassignedTrips

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking

    fun loadUnassignedTrips() {
        viewModelScope.launch {
            _unassignedTrips.value = db.tripDao().getUnassignedTrips()
        }
    }

    fun assignTripToCar(tripId: Long, carId: Int, numberPlate: String) {
        viewModelScope.launch {
            db.tripDao().assignTripToCar(tripId, carId, numberPlate)
            loadUnassignedTrips()
        }
    }

    fun updateTripDistance(trip: Trip, newDistance: Double) {
        viewModelScope.launch {
            val duration = trip.duration
            val avgSpeed = if (duration > 0) newDistance / duration else 0.0

            val updatedTrip = trip.copy(
                distance = newDistance,
                averageSpeed = avgSpeed
            )
            db.tripDao().updateTrip(updatedTrip)
            loadUnassignedTrips()
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            db.tripDao().deleteTrip(trip)
            db.gpsPointDao().deletePointsByTrip(trip.id)
            loadUnassignedTrips()
        }
    }

    fun deleteAllTrips(trips: List<Trip>) {
        viewModelScope.launch {
            trips.forEach { trip ->
                db.tripDao().deleteTrip(trip)
                db.gpsPointDao().deletePointsByTrip(trip.id)
            }
            loadUnassignedTrips()
        }
    }

    fun editTrip(trip: Trip, newStartTime: Date, newEndTime: Date, newDistance: Double) {
        viewModelScope.launch {
            val updatedTrip = trip.copy(
                startTime = newStartTime,
                endTime = newEndTime,
                distance = newDistance,
                duration = (newEndTime.time - newStartTime.time) / 1000,
                averageSpeed = if (newDistance > 0 && (newEndTime.time - newStartTime.time) > 0)
                    newDistance / ((newEndTime.time - newStartTime.time) / 1000) else 0.0
            )
            db.tripDao().updateTrip(updatedTrip)
            loadUnassignedTrips()
        }
    }
}