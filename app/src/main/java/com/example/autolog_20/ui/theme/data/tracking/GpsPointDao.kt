package com.example.autolog_20.ui.theme.data.tracking

import androidx.room.*

@Dao
interface GpsPointDao {
    @Insert
    suspend fun insertPoint(point: GpsPoint)

    @Query("SELECT * FROM gps_points WHERE tripId = :tripId ORDER BY timestamp ASC")
    suspend fun getPointsByTrip(tripId: Long): List<GpsPoint>

    @Query("DELETE FROM gps_points WHERE tripId = :tripId")
    suspend fun deletePointsByTrip(tripId: Long)
}