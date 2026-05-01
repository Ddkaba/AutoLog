package com.example.autolog_20.ui.theme.data.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.autolog_20.ui.theme.data.api.AuthApi
import com.example.autolog_20.ui.theme.data.api.RetrofitClient
import com.example.autolog_20.ui.theme.data.locale.TokenManager
import com.example.autolog_20.ui.theme.data.network.AndroidConnectivityObserver
import com.example.autolog_20.ui.theme.data.network.isServerReachable

@Composable
fun StartupScreen(
    navController: NavController,
    authApi: AuthApi = RetrofitClient.api
) {
    val context = LocalContext.current

    val connectivityObserver = remember {
        AndroidConnectivityObserver(context)
    }

    val isOnline by connectivityObserver.isConnected
        .collectAsStateWithLifecycle(
            initialValue = connectivityObserver.isCurrentlyConnected()
        )

    var serverAvailable by remember { mutableStateOf<Boolean?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isOnline) {
        if (isOnline) {
            serverAvailable = authApi.isServerReachable()
            if (serverAvailable == true) {
                if (TokenManager.isLoggedIn()) {
                    navController.navigate("main") {
                        popUpTo("startup") { inclusive = true }
                    }
                } else {
                    navController.navigate("login") {
                        popUpTo("startup") { inclusive = true }
                    }
                }
            } else {
                errorMessage = "Сервер недоступен. Проверьте интернет или попробуйте позже."
            }
        } else {
            errorMessage = "Нет подключения к интернету"
        }
    }

    if (errorMessage != null) {
        NoConnectionScreen(
            message = errorMessage!!,
            onRetry = {
                errorMessage = null
                serverAvailable = null
            }
        )
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = if (isOnline) "Загрузка..." else "Загрузка...",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
