package com.example.autolog_20.ui.theme.data.tracking

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.autolog_20.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class SimpleTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Переменные для отслеживания движения
    private var lastLatitude: Double? = null
    private var lastLongitude: Double? = null
    private var totalDistance = 0.0
    private var tripPoints = mutableListOf<Location>()

    // Состояние поездки
    private var isTripInProgress = false
    private var tripStartTime: Long = 0

    // Для проверки остановки (5 минут без движения в радиусе 250м)
    private var lastMovingLocation: Location? = null
    private var stationaryCheckStartTime: Long = 0

    companion object {
        private const val CHANNEL_ID = "simple_tracking_channel"
        private const val NOTIFICATION_ID = 100
        private const val UPDATE_INTERVAL = 15000L // 15 секунд

        // Параметры для определения поездки
        private const val MIN_SPEED_FOR_TRIP = 20.0 // 20 км/ч - начало поездки
        private const val STATIONARY_TIMEOUT = 300000L // 5 минут для остановки
        private const val STATIONARY_RADIUS = 250.0 // 250 метров радиус для определения остановки

        fun start(context: Context) {
            val intent = Intent(context, SimpleTrackingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, SimpleTrackingService::class.java)
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
                "Отслеживание геолокации",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Автоматическое отслеживание поездок на автомобиле"
                setSound(null, null)
                enableVibration(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL
        ).apply {
            setMinUpdateIntervalMillis(UPDATE_INTERVAL)
            setMaxUpdateDelayMillis(UPDATE_INTERVAL * 2)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    processLocationUpdate(location)
                }
            }
        }
    }

    private fun processLocationUpdate(location: Location) {
        val speedKmh = location.speed * 3.6
        val isMovingBySpeed = speedKmh >= MIN_SPEED_FOR_TRIP

        android.util.Log.d("SimpleTracking", """
            📍 Обновление:
            Скорость: ${String.format("%.1f", speedKmh)} км/ч
            Порог: $MIN_SPEED_FOR_TRIP км/ч
            Движение: ${if (isMovingBySpeed) "ДА" else "НЕТ"}
            Поездка в процессе: $isTripInProgress
        """.trimIndent())

        if (isMovingBySpeed) {
            // Движемся достаточно быстро - АВТОМОБИЛЬ
            if (!isTripInProgress) {
                startNewTrip(location)
            } else {
                continueTrip(location)
            }
            // Сбрасываем таймеры остановки
            stationaryCheckStartTime = 0
            lastMovingLocation = location
        } else {
            // Скорость недостаточна для автомобиля
            if (isTripInProgress) {
                // Поездка в процессе, проверяем остановку
                checkIfTripShouldEnd(location)
            }
        }

        updateNotification(location, isTripInProgress)
    }

    private fun startNewTrip(location: Location) {
        isTripInProgress = true
        tripStartTime = System.currentTimeMillis()
        totalDistance = 0.0
        tripPoints.clear()
        tripPoints.add(location)
        lastLatitude = location.latitude
        lastLongitude = location.longitude
        lastMovingLocation = location
        stationaryCheckStartTime = 0

        android.util.Log.d("SimpleTracking", """
            🚗🚗🚗 НАЧАЛО ПОЕЗДКИ! 🚗🚗🚗
            Скорость: ${location.speed * 3.6} км/ч
            Время: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}
            Старт: ${location.latitude}, ${location.longitude}
        """.trimIndent())
    }

    private fun continueTrip(location: Location) {
        tripPoints.add(location)

        // Рассчитываем расстояние от последней точки
        lastLatitude?.let { lat ->
            lastLongitude?.let { lon ->
                val distance = calculateDistance(lat, lon, location.latitude, location.longitude)
                if (distance > 5.0) {
                    totalDistance += distance
                    android.util.Log.d("SimpleTracking", "📏 +${String.format("%.1f", distance)} м | Всего: ${String.format("%.2f", totalDistance / 1000)} км | Точек: ${tripPoints.size}")
                }
            }
        }

        lastLatitude = location.latitude
        lastLongitude = location.longitude
        lastMovingLocation = location
        stationaryCheckStartTime = 0 // Сбрасываем таймер при движении
    }

    private fun checkIfTripShouldEnd(currentLocation: Location) {
        if (lastMovingLocation == null) {
            lastMovingLocation = currentLocation
            stationaryCheckStartTime = System.currentTimeMillis()
            return
        }

        // Рассчитываем расстояние от последнего места движения
        val distanceFromLastMove = calculateDistance(
            lastMovingLocation!!.latitude, lastMovingLocation!!.longitude,
            currentLocation.latitude, currentLocation.longitude
        )

        val stationaryDuration = System.currentTimeMillis() - stationaryCheckStartTime

        android.util.Log.d("SimpleTracking", """
            ⏸️ Проверка остановки:
            Расстояние от последнего движения: ${String.format("%.1f", distanceFromLastMove)} м
            Лимит радиуса: $STATIONARY_RADIUS м
            Время остановки: ${stationaryDuration / 1000} сек
            Требуется: ${STATIONARY_TIMEOUT / 1000} сек
        """.trimIndent())

        if (distanceFromLastMove <= STATIONARY_RADIUS) {
            // В пределах радиуса - считаем что стоим
            if (stationaryCheckStartTime == 0L) {
                stationaryCheckStartTime = System.currentTimeMillis()
            } else if (stationaryDuration >= STATIONARY_TIMEOUT) {
                // Прошло 5 минут без значительного движения
                android.util.Log.d("SimpleTracking", "✅ Условия остановки выполнены: ${STATIONARY_TIMEOUT/1000} минут в радиусе ${STATIONARY_RADIUS}м")
                finishTrip(currentLocation)
            }
        } else {
            // Сдвинулись больше чем на 250м - продолжаем поездку
            android.util.Log.d("SimpleTracking", "🚗 Обнаружено движение! Продолжаем поездку...")
            stationaryCheckStartTime = 0
            lastMovingLocation = currentLocation
            continueTrip(currentLocation)
        }
    }

    private fun finishTrip(endLocation: Location) {
        val duration = (System.currentTimeMillis() - tripStartTime) / 1000
        val durationMinutes = duration / 60
        val durationSeconds = duration % 60
        val avgSpeed = if (duration > 0) (totalDistance / duration) * 3.6 else 0.0
        val maxSpeed = tripPoints.maxOfOrNull { it.speed * 3.6 } ?: 0.0

        android.util.Log.d("SimpleTracking", """
            🏁🏁🏁 ЗАВЕРШЕНИЕ ПОЕЗДКИ! 🏁🏁🏁
            =================================
            📊 СТАТИСТИКА:
            Расстояние: ${String.format("%.2f", totalDistance / 1000)} км
            Время: ${durationMinutes} мин ${durationSeconds} сек
            Средняя скорость: ${String.format("%.1f", avgSpeed)} км/ч
            Максимальная скорость: ${String.format("%.1f", maxSpeed)} км/ч
            Точек маршрута: ${tripPoints.size}
            =================================
        """.trimIndent())

        // Сохраняем в базу данных
        saveTripToDatabase(endLocation, duration, avgSpeed, maxSpeed)

        isTripInProgress = false
        stationaryCheckStartTime = 0
    }

    private fun saveTripToDatabase(endLocation: Location, duration: Long, avgSpeed: Double, maxSpeed: Double) {
        serviceScope.launch {
            try {
                val db = TripDatabase.getDatabase(applicationContext)

                // Сохраняем информацию о поездке
                val trip = Trip(
                    startTime = Date(tripStartTime),
                    endTime = Date(),
                    distance = totalDistance,
                    duration = duration,
                    startLat = tripPoints.firstOrNull()?.latitude ?: 0.0,
                    startLon = tripPoints.firstOrNull()?.longitude ?: 0.0,
                    endLat = endLocation.latitude,
                    endLon = endLocation.longitude,
                    maxSpeed = maxSpeed,
                    averageSpeed = avgSpeed,
                    isCompleted = true,
                    isAssigned = false,
                    isSynced = false
                )

                val tripId = db.tripDao().insertTrip(trip)
                android.util.Log.d("SimpleTracking", "✅ Поездка сохранена! ID: $tripId")

                // Сохраняем GPS точки маршрута
                var pointsSaved = 0
                tripPoints.forEach { point ->
                    val gpsPoint = GpsPoint(
                        tripId = tripId,
                        timestamp = Date(point.time),
                        latitude = point.latitude,
                        longitude = point.longitude,
                        accuracy = point.accuracy,
                        speed = point.speed,
                        altitude = point.altitude
                    )
                    db.gpsPointDao().insertPoint(gpsPoint)
                    pointsSaved++
                }

                android.util.Log.d("SimpleTracking", "✅ Сохранено $pointsSaved GPS точек")

            } catch (e: Exception) {
                android.util.Log.e("SimpleTracking", "❌ Ошибка сохранения: ${e.message}")
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    private fun updateNotification(location: Location, isActiveTrip: Boolean) {
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timeString = dateFormat.format(Date())
        val speedKmh = location.speed * 3.6

        val notificationText = if (isActiveTrip) {
            """
            🚗 ПОЕЗДКА В ПРОГРЕССЕ
            
            Время: $timeString
            Скорость: ${String.format("%.1f", speedKmh)} км/ч
            
            📊 Статистика:
            Пройдено: ${String.format("%.2f", totalDistance / 1000)} км
            Время: ${(System.currentTimeMillis() - tripStartTime) / 60000} мин
            """.trimIndent()
        } else {
            """
            📍 ОЖИДАНИЕ ДВИЖЕНИЯ
            
            Время: $timeString
            Для начала поездки: скорость > $MIN_SPEED_FOR_TRIP км/ч
            Для остановки: 5 минут в радиусе 250 м
            """.trimIndent()
        }

        val shortText = if (isActiveTrip) {
            "🚗 ${String.format("%.1f", speedKmh)} км/ч | ${String.format("%.2f", totalDistance / 1000)} км"
        } else {
            "📍 Ожидание скорости >$MIN_SPEED_FOR_TRIP км/ч"
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (isActiveTrip) "🚗 Поездка" else "📍 Отслеживание")
            .setContentText(shortText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManagerCompat = NotificationManagerCompat.from(this)
        if (notificationManagerCompat.areNotificationsEnabled()) {
            notificationManagerCompat.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun startForegroundWithDefaultNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("📍 Отслеживание геолокации")
            .setContentText("Ожидание скорости >$MIN_SPEED_FOR_TRIP км/ч")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startForegroundWithDefaultNotification()
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            android.util.Log.d("SimpleTracking", "✅ GPS трекер запущен")
            android.util.Log.d("SimpleTracking", "📋 Настройки: >$MIN_SPEED_FOR_TRIP км/ч для начала, ${STATIONARY_TIMEOUT/1000} мин/250м для остановки")
        } else {
            android.util.Log.d("SimpleTracking", "❌ Нет разрешения на локацию")
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        android.util.Log.d("SimpleTracking", "▶️ Сервис запущен")
        startLocationUpdates()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d("SimpleTracking", "🔴 Сервис остановлен")
        if (::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}