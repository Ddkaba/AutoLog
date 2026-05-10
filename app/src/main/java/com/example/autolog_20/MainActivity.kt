package com.example.autolog_20

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.autolog_20.ui.theme.AutoLogTheme
import com.example.autolog_20.ui.theme.BackgroundDark
import com.example.autolog_20.ui.theme.data.locale.LocaleManager
import com.example.autolog_20.ui.theme.data.locale.TokenManager
import com.example.autolog_20.ui.theme.data.screen.AddCarByScanStsScreen
import com.example.autolog_20.ui.theme.data.screen.AddCarByVinScreen
import com.example.autolog_20.ui.theme.data.screen.AddCarManualScreen
import com.example.autolog_20.ui.theme.data.screen.CarDetailsScreen
import com.example.autolog_20.ui.theme.data.screen.ExpensesScreen
import com.example.autolog_20.ui.theme.data.screen.LoginScreen
import com.example.autolog_20.ui.theme.data.screen.MainScreen
import com.example.autolog_20.ui.theme.data.screen.MaintenanceScreen
import com.example.autolog_20.ui.theme.data.screen.RegisterScreen
import com.example.autolog_20.ui.theme.data.screen.SettingsScreen
import com.example.autolog_20.ui.theme.data.screen.StartupScreen
import com.example.autolog_20.ui.theme.data.locale.SettingsManager
import com.example.autolog_20.ui.theme.data.model.viewmodel.TripsViewModel
import com.example.autolog_20.ui.theme.data.model.STSRecognitionData
import com.example.autolog_20.ui.theme.data.screen.AddCarByScanDataScreen
import com.example.autolog_20.ui.theme.data.screen.AddCarFromSTSScreen
import com.example.autolog_20.ui.theme.data.screen.MileageScreen
import com.example.autolog_20.ui.theme.data.screen.ServicesScreen
import com.example.autolog_20.ui.theme.data.tracking.SimpleTrackingService

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        SettingsManager.init(newBase)
        val language = SettingsManager.getLanguage()
        super.attachBaseContext(LocaleManager.setLocale(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AutoLogApplication.currentActivity = this
        createNotificationChannel()
        TokenManager.init(this)

        val tripsViewModel = TripsViewModel(this)

        if (SettingsManager.isGpsMileageEnabled()) {
            SimpleTrackingService.start(this)
        }

        setContent {
            AutoLogTheme {
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "startup"
                    ) {
                        composable("startup") { StartupScreen(navController) }
                        composable("login") { LoginScreen(navController) }
                        composable("register") { RegisterScreen(navController) }
                        composable("main") { MainScreen(navController) }


                        composable("add_car_vin") { AddCarByVinScreen(navController) }
                        composable("add_car_manual") { AddCarManualScreen(navController) }
                        composable("add_car_scan_sts") { AddCarByScanStsScreen(navController) }

                        composable(
                            route = "add_car_manual_with_data_confirm/{vin}/{brand}/{model}/{year}/{color}/{numberPlate}",
                            arguments = listOf(
                                navArgument("vin") { type = NavType.StringType },
                                navArgument("brand") { type = NavType.StringType },
                                navArgument("model") { type = NavType.StringType },
                                navArgument("year") { type = NavType.IntType },
                                navArgument("color") { type = NavType.StringType },
                                navArgument("numberPlate") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val vin = backStackEntry.arguments?.getString("vin") ?: ""
                            val brand = backStackEntry.arguments?.getString("brand") ?: ""
                            val model = backStackEntry.arguments?.getString("model") ?: ""
                            val year = backStackEntry.arguments?.getInt("year") ?: 0
                            val color = backStackEntry.arguments?.getString("color") ?: ""
                            val numberPlate = backStackEntry.arguments?.getString("numberPlate") ?: ""

                            AddCarByScanDataScreen(
                                navController = navController,
                                vin = vin,
                                brand = brand,
                                model = model,
                                year = year,
                                color = color,
                                numberPlate = numberPlate
                            )
                        }

                        composable(
                            route = "add_car_from_sts/{vin}/{brand}/{model}/{year}/{color}/{numberPlate}",
                            arguments = listOf(
                                navArgument("vin") { type = NavType.StringType },
                                navArgument("brand") { type = NavType.StringType },
                                navArgument("model") { type = NavType.StringType },
                                navArgument("year") { type = NavType.IntType },
                                navArgument("color") { type = NavType.StringType },
                                navArgument("numberPlate") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val vin = backStackEntry.arguments?.getString("vin") ?: ""
                            val brand = backStackEntry.arguments?.getString("brand") ?: ""
                            val model = backStackEntry.arguments?.getString("model") ?: ""
                            val year = backStackEntry.arguments?.getInt("year") ?: 0
                            val color = backStackEntry.arguments?.getString("color") ?: ""
                            val numberPlate = backStackEntry.arguments?.getString("numberPlate") ?: ""

                            val stsData = STSRecognitionData(
                                vin = vin,
                                brand = brand,
                                model = model,
                                year = year,
                                color = color,
                                numberPlate = numberPlate
                            )

                            AddCarFromSTSScreen(
                                navController = navController,
                                stsData = stsData
                            )
                        }

                        composable(route = "car_details/{numberPlate}",
                            arguments = listOf(navArgument("numberPlate") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val numberPlate = backStackEntry.arguments?.getString("numberPlate") ?: ""
                            if (numberPlate.isNotBlank()) {
                                CarDetailsScreen(navController, numberPlate)
                            } else {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Номер автомобиля не указан")
                                }
                            }
                        }

                        composable("maintenance/{numberPlate}") { backStackEntry ->
                            val numberPlate = backStackEntry.arguments?.getString("numberPlate") ?: ""
                            MaintenanceScreen(navController, numberPlate)
                        }

                        composable("expenses/{numberPlate}") { backStackEntry ->
                            val numberPlate = backStackEntry.arguments?.getString("numberPlate") ?: ""
                            ExpensesScreen(
                                navController = navController,
                                numberPlate = numberPlate
                            )
                        }

                        composable("mileage/{numberPlate}") { backStackEntry ->
                            val numberPlate = backStackEntry.arguments?.getString("numberPlate") ?: ""
                            MileageScreen(navController, numberPlate)
                        }

                        composable(
                            route = "services/{numberPlate}/{lat}/{lon}",
                            arguments = listOf(
                                navArgument("numberPlate") { type = NavType.StringType },
                                navArgument("lat") { type = NavType.FloatType },
                                navArgument("lon") { type = NavType.FloatType }
                            )
                        ) { backStackEntry ->
                            val numberPlate = backStackEntry.arguments?.getString("numberPlate") ?: ""
                            val lat = backStackEntry.arguments?.getFloat("lat") ?: 0f
                            val lon = backStackEntry.arguments?.getFloat("lon") ?: 0f

                            ServicesScreen(
                                navController = navController,
                                currentLat = lat.toDouble(),
                                currentLon = lon.toDouble()
                            )
                        }

                        composable("settings") {
                            SettingsScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (AutoLogApplication.currentActivity == this) {
            AutoLogApplication.currentActivity = null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "autolog_channel"
            val channelName = "AutoLog Notifications"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о рекомендациях"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun requestLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                1001
            )
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        }
    }

}