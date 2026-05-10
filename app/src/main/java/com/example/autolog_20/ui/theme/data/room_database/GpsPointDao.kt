package com.example.autolog_20.ui.theme.data.room_database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.autolog_20.ui.theme.data.model.GpsPoint

@Dao
interface GpsPointDao {
    @Insert
    suspend fun insertPoint(point: GpsPoint)

    @Query("SELECT * FROM gps_points WHERE tripId = :tripId ORDER BY timestamp ASC")
    suspend fun getPointsByTrip(tripId: Long): List<GpsPoint>

    @Query("DELETE FROM gps_points WHERE tripId = :tripId")
    suspend fun deletePointsByTrip(tripId: Long)
}