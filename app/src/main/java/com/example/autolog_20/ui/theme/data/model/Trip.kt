package com.example.autolog_20.ui.theme.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val carId: Int? = null,
    val carNumberPlate: String? = null,
    val startTime: Date,
    val endTime: Date? = null,
    val distance: Double = 0.0,
    val duration: Long = 0,
    val startLat: Double = 0.0,
    val startLon: Double = 0.0,
    val endLat: Double = 0.0,
    val endLon: Double = 0.0,
    val maxSpeed: Double = 0.0,
    val averageSpeed: Double = 0.0,
    val isSynced: Boolean = false,
    val isAssigned: Boolean = false,
    val isCompleted: Boolean = false,
    val routePoints: String = ""
)

@Entity(tableName = "gps_points")
data class GpsPoint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripId: Long,
    val timestamp: Date,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val speed: Float,
    val altitude: Double
)