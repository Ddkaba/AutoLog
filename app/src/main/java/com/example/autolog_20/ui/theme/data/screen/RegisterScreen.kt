package com.example.autolog_20.ui.theme.data.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.autolog_20.ui.theme.data.api.RetrofitClient
import com.example.autolog_20.ui.theme.data.register.RegisterUiState
import com.example.autolog_20.ui.theme.data.register.RegisterViewModel
import kotlinx.coroutines.delay

@Composable
fun RegisterScreen(
    navController: NavController
) {
    val viewModel: RegisterViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RegisterViewModel(RetrofitClient.api) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        when (uiState) {
            is RegisterUiState.Success -> {
                snackbarHostState.showSnackbar(
                    message = "Аккаунт создан. Перенаправление на вход…",
                    duration = SnackbarDuration.Short
                )
                delay(1800)
                navController.navigate("login") {
                    popUpTo("register") { inclusive = true }
                }
            }
            is RegisterUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (uiState as RegisterUiState.Error).message,
                    duration = SnackbarDuration.Long
                )
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = "Регистрация",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // USERNAME
            OutlinedTextField(
                value = viewModel.username,
                onValueChange = viewModel::onUsernameChanged,
                label = { Text("Имя пользователя") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState is RegisterUiState.ValidationError &&
                        (uiState as RegisterUiState.ValidationError).usernameError != null,
                supportingText = {
                    if (uiState is RegisterUiState.ValidationError) {
                        (uiState as RegisterUiState.ValidationError)
                            .usernameError
                            ?.let { Text(it) }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // EMAIL
            OutlinedTextField(
                value = viewModel.email,
                onValueChange = viewModel::onEmailChanged,
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                isError = uiState is RegisterUiState.ValidationError &&
                        (uiState as RegisterUiState.ValidationError).emailError != null,
                supportingText = {
                    if (uiState is RegisterUiState.ValidationError) {
                        (uiState as RegisterUiState.ValidationError)
                            .emailError
                            ?.let { Text(it) }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // PASSWORD
            OutlinedTextField(
                value = viewModel.password,
                onValueChange = viewModel::onPasswordChanged,
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                isError = uiState is RegisterUiState.ValidationError &&
                        (uiState as RegisterUiState.ValidationError).passwordError != null,
                supportingText = {
                    if (uiState is RegisterUiState.ValidationError) {
                        (uiState as RegisterUiState.ValidationError)
                            .passwordError
                            ?.let { Text(it) }
                    } else {
                        Text("Минимум 8 символов, цифра и заглавная буква")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CONFIRM PASSWORD
            OutlinedTextField(
                value = viewModel.passwordConfirm,
                onValueChange = viewModel::onPasswordConfirmChanged,
                label = { Text("Повторите пароль") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                isError = uiState is RegisterUiState.ValidationError &&
                        (uiState as RegisterUiState.ValidationError).confirmError != null,
                supportingText = {
                    if (uiState is RegisterUiState.ValidationError) {
                        (uiState as RegisterUiState.ValidationError)
                            .confirmError
                            ?.let { Text(it) }
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = viewModel::register,
                enabled = uiState !is RegisterUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState is RegisterUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Создать аккаунт")
                }
            }
        }
    }
}
