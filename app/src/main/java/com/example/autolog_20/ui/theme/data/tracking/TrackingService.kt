package com.example.autolog_20.ui.theme.data.tracking

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.autolog_20.ui.theme.data.model.GpsPoint
import com.example.autolog_20.ui.theme.data.model.Trip
import com.example.autolog_20.ui.theme.data.room_database.TripDatabase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class TrackingService : Service() {

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking

    private val _currentDistance = MutableStateFlow(0.0)
    val currentDistance: StateFlow<Double> = _currentDistance

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var currentTripId: Long = 0
    private var lastLocation: Location? = null
    private var tripPoints = mutableListOf<Location>()
    private var isMoving = false
    private var stationaryStartTime: Long = 0

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 1
        private const val MIN_DISTANCE = 10.0  // 10 метров минимальное расстояние для записи
        private const val STATIONARY_TIMEOUT = 120000L  // 2 минуты без движения = остановка

        fun start(context: Context) {
            val intent = Intent(context, TrackingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, TrackingService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupLocationClient()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Отслеживание пробега",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Уведомления о статусе отслеживания пробега"
                setSound(null, null)
            }
            // ИСПРАВЛЕНО: используем getSystemService() через applicationContext
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L  // 5 секунд интервал
        ).apply {
            setMinUpdateIntervalMillis(2000L)  // 2 секунды минимальный интервал
            setMaxUpdateDelayMillis(30000L)    // 30 секунд макс задержка
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    handleLocationUpdate(location)
                }
            }
        }
    }

    private fun handleLocationUpdate(location: Location) {
        if (location.accuracy > 30) return

        val isCurrentlyMoving = location.speed > 2.0

        if (isCurrentlyMoving) {
            if (!isMoving) {
                // Начало движения
                isMoving = true
                startNewTrip(location)
            }

            // Добавляем точку к текущей поездке
            tripPoints.add(location)

            // Рассчитываем расстояние от последней точки
            lastLocation?.let { last ->
                val distance = haversineDistance(
                    last.latitude, last.longitude,
                    location.latitude, location.longitude
                )
                if (distance > MIN_DISTANCE) {
                    _currentDistance.value += distance
                }
            }

            lastLocation = location
        } else {
            // Стоим
            if (isMoving) {
                if (stationaryStartTime == 0L) {
                    stationaryStartTime = System.currentTimeMillis()
                } else if (System.currentTimeMillis() - stationaryStartTime > STATIONARY_TIMEOUT) {
                    // Остановка: завершаем поездку
                    finishCurrentTrip(location)
                    isMoving = false
                    stationaryStartTime = 0L
                }
            }
        }

        // Обновляем уведомление
        updateNotification()
    }

    private fun startNewTrip(startLocation: Location) {
        currentTripId = System.currentTimeMillis()
        _currentDistance.value = 0.0
        tripPoints.clear()
        tripPoints.add(startLocation)
        lastLocation = startLocation

        // Сохраняем начальную точку в БД
        scope.launch {
            // ИСПРАВЛЕНО: добавлен параметр startTime
            val trip = Trip(
                startTime = Date(startLocation.time),
                startLat = startLocation.latitude,
                startLon = startLocation.longitude,
                isCompleted = false
            )
            currentTripId = TripDatabase.getDatabase(applicationContext)
                .tripDao()
                .insertTrip(trip)
        }

        _isTracking.value = true
    }

    private fun finishCurrentTrip(endLocation: Location) {
        if (currentTripId == 0L || tripPoints.isEmpty()) return

        scope.launch {
            val startTime = tripPoints.first().time
            val endTime = endLocation.time
            val duration = (endTime - startTime) / 1000

            // Рассчитываем среднюю скорость
            val avgSpeed = if (duration > 0) _currentDistance.value / duration else 0.0

            // Находим максимальную скорость
            val maxSpeed = tripPoints.maxOfOrNull { it.speed.toDouble() } ?: 0.0

            // Сохраняем точки маршрута как JSON
            val routePointsJson = tripPoints.map { point ->
                mapOf(
                    "lat" to point.latitude,
                    "lon" to point.longitude,
                    "time" to point.time,
                    "speed" to point.speed
                )
            }.toString()

            val trip = Trip(
                id = currentTripId,
                startTime = Date(startTime),  // ИСПРАВЛЕНО: добавлен startTime
                endTime = Date(endTime),
                distance = _currentDistance.value,
                duration = duration,
                endLat = endLocation.latitude,
                endLon = endLocation.longitude,
                maxSpeed = maxSpeed,
                averageSpeed = avgSpeed,
                isCompleted = true,
                routePoints = routePointsJson
            )

            TripDatabase.getDatabase(applicationContext).tripDao().updateTrip(trip)

            // Сохраняем GPS точки
            tripPoints.forEach { point ->
                val gpsPoint = GpsPoint(
                    tripId = currentTripId,
                    timestamp = Date(point.time),
                    latitude = point.latitude,
                    longitude = point.longitude,
                    accuracy = point.accuracy,
                    speed = point.speed,
                    altitude = point.altitude
                )
                TripDatabase.getDatabase(applicationContext).gpsPointDao().insertPoint(gpsPoint)
            }
        }

        _currentDistance.value = 0.0
        _isTracking.value = false
        updateNotification(completed = true)
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0  // Радиус Земли в метрах
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    private fun updateNotification(completed: Boolean = false) {
        val notificationText = if (completed) {
            "Поездка завершена. Пройдено ${String.format("%.1f", _currentDistance.value / 1000)} км"
        } else if (isMoving) {
            "В пути: ${String.format("%.1f", _currentDistance.value / 1000)} км"
        } else {
            "Ожидание движения..."
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Отслеживание пробега")
            .setContentText(notificationText)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(isMoving)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}