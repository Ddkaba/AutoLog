package com.example.autolog_20

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.autolog_20.ui.theme.AutoLogTheme
import com.example.autolog_20.ui.theme.BackgroundDark
import com.example.autolog_20.ui.theme.data.locale.TokenManager
import com.example.autolog_20.ui.theme.data.screen.AddCarScreen
import com.example.autolog_20.ui.theme.data.screen.LoginScreen
import com.example.autolog_20.ui.theme.data.screen.MainScreen
import com.example.autolog_20.ui.theme.data.screen.RegisterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                        startDestination = if (TokenManager.isLoggedIn()) "main" else "login"
                    ) {
                        composable("login") { LoginScreen(navController) }
                        composable("register") { RegisterScreen(navController) }
                        composable("main") { MainScreen(navController) }
                        composable("add_car") { AddCarScreen(navController) }
                    }
                }
            }
        }
    }
}
