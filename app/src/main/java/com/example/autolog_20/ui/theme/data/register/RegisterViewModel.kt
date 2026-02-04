package com.example.autolog_20.ui.theme.data.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autolog_20.ui.theme.data.api.AuthApi
import com.example.autolog_20.ui.theme.data.model.RegisterRequest
import com.example.autolog_20.ui.theme.data.model.RegisterResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class RegisterViewModel(
    private val authApi: AuthApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Initial)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    var username by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var passwordConfirm by mutableStateOf("")
        private set

    fun onUsernameChanged(value: String) {
        username = value
        _uiState.update { it.copyValidationErrors() }
    }

    fun onEmailChanged(value: String) {
        email = value
        _uiState.update { it.copyValidationErrors() }
    }

    fun onPasswordChanged(value: String) {
        password = value
        _uiState.update { it.copyValidationErrors() }
    }

    fun onPasswordConfirmChanged(value: String) {
        passwordConfirm = value
        _uiState.update { it.copyValidationErrors() }
    }

    fun register() {
        viewModelScope.launch {
            val errors = validate()
            if (errors.isNotEmpty()) {
                _uiState.value = RegisterUiState.ValidationError(
                    usernameError = errors["username"],
                    emailError   = errors["email"],
                    passwordError = errors["password"],
                    confirmError = errors["confirm"]
                )
                return@launch
            }

            _uiState.value = RegisterUiState.Loading

            try {
                val response: Response<RegisterResponse> = authApi.register(
                    RegisterRequest(
                        username = username.trim(),
                        email = email.trim(),
                        password = password
                    )
                )

                if (response.isSuccessful) {
                    _uiState.value = RegisterUiState.Success
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = try {
                        JSONObject(errorBody ?: "{}").optString("error", "Ошибка регистрации")
                    } catch (e: Exception) {
                        "Ошибка сервера (${response.code()})"
                    }
                    _uiState.value = RegisterUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState.Error("Ошибка сети: ${e.localizedMessage ?: "неизвестно"}")
            }
        }
    }

    private fun validate(): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (username.trim().isEmpty()) {
            errors["username"] = "Введите имя пользователя"
        } else if (username.length < 3) {
            errors["username"] = "Минимум 3 символа"
        }

        if (email.trim().isEmpty()) {
            errors["email"] = "Введите email"
        } else if (!isValidEmail(email)) {
            errors["email"] = "Некорректный email"
        }

        if (password.isEmpty()) {
            errors["password"] = "Введите пароль"
        } else if (password.length < 8) {
            errors["password"] = "Минимум 8 символов"
        } else if (!password.any { it.isDigit() }) {
            errors["password"] = "Добавьте хотя бы одну цифру"
        } else if (!password.any { it.isUpperCase() }) {
            errors["password"] = "Добавьте заглавную букву"
        }

        if (passwordConfirm != password) {
            errors["confirm"] = "Пароли не совпадают"
        }

        return errors
    }

    private fun RegisterUiState.copyValidationErrors(): RegisterUiState {
        return when (this) {
            is RegisterUiState.ValidationError -> this
            else -> RegisterUiState.Initial
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

sealed interface RegisterUiState {
    data object Initial : RegisterUiState
    data object Loading : RegisterUiState
    data object Success : RegisterUiState

    data class ValidationError(
        val usernameError: String? = null,
        val emailError: String? = null,
        val passwordError: String? = null,
        val confirmError: String? = null
    ) : RegisterUiState

    data class Error(val message: String) : RegisterUiState
}