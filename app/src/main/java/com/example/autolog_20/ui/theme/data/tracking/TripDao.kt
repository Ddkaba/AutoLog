package com.example.autolog_20.ui.theme.data.tracking

import androidx.room.*

@Dao
interface TripDao {
    @Insert
    suspend fun insertTrip(trip: Trip): Long

    @Update
    suspend fun updateTrip(trip: Trip)

    @Query("SELECT * FROM trips WHERE isCompleted = 1 AND isAssigned = 0 ORDER BY startTime DESC")
    suspend fun getUnassignedTrips(): List<Trip>

    @Query("SELECT * FROM trips WHERE carId = :carId AND isCompleted = 1 ORDER BY startTime DESC")
    suspend fun getTripsByCar(carId: Int): List<Trip>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripById(tripId: Long): Trip?

    @Query("UPDATE trips SET carId = :carId, carNumberPlate = :numberPlate, isAssigned = 1 WHERE id = :tripId")
    suspend fun assignTripToCar(tripId: Long, carId: Int, numberPlate: String)

    @Query("SELECT * FROM trips WHERE isSynced = 0 AND isCompleted = 1 AND isAssigned = 1")
    suspend fun getUnsyncedTrips(): List<Trip>

    @Query("UPDATE trips SET isSynced = 1 WHERE id = :tripId")
    suspend fun markAsSynced(tripId: Long)

    @Delete
    suspend fun deleteTrip(trip: Trip)

    @Query("SELECT * FROM trips")
    suspend fun getAllTrips(): List<Trip>
}