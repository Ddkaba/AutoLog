package com.example.autolog_20

import android.app.NotificationChannel
import android.app.NotificationManager
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
import com.example.autolog_20.ui.theme.data.locale.TokenManager
import com.example.autolog_20.ui.theme.data.screen.AddCarByScanStsScreen
import com.example.autolog_20.ui.theme.data.screen.AddCarByVinScreen
import com.example.autolog_20.ui.theme.data.screen.AddCarManualScreen
import com.example.autolog_20.ui.theme.data.screen.CarDetailsScreen
import com.example.autolog_20.ui.theme.data.screen.ExpensesScreen
import com.example.autolog_20.ui.theme.data.screen.LoginScreen
import com.example.autolog_20.ui.theme.data.screen.MainScreen
import com.example.autolog_20.ui.theme.data.screen.RegisterScreen
import com.example.autolog_20.ui.theme.data.screen.SettingsScreen  // Добавьте этот импорт
import com.example.autolog_20.ui.theme.data.screen.StartupScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        TokenManager.init(this)

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
                        composable("add_car_scan_sts") { AddCarByScanStsScreen(navController) }
                        composable("add_car_manual") { AddCarManualScreen(navController) }

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

                        composable("maintenance/{numberPlate}") { /* MaintenanceScreen */ }
                        composable("tires/{numberPlate}") { /* TiresScreen */ }

                        composable("expenses/{numberPlate}") { backStackEntry ->
                            val numberPlate = backStackEntry.arguments?.getString("numberPlate") ?: ""
                            ExpensesScreen(
                                navController = navController,
                                numberPlate = numberPlate
                            )
                        }

                        composable("mileage/{numberPlate}") { /* MileageScreen */ }
                        composable("services/{numberPlate}") { /* ServicesScreen */ }

                        composable("settings") {
                            SettingsScreen(navController = navController)
                        }
                    }
                }
            }
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
}