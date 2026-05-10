package com.example.autolog_20.ui.theme.data.room_database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.autolog_20.ui.theme.data.model.Trip
import com.example.autolog_20.ui.theme.data.model.GpsPoint

@Database(
    entities = [Trip::class, GpsPoint::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TripDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun gpsPointDao(): GpsPointDao

    companion object {
        @Volatile
        private var INSTANCE: TripDatabase? = null

        fun getDatabase(context: Context): TripDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TripDatabase::class.java,
                    "trips_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}