package com.example.autolog_20.ui.theme.data.model.viewmodel

import androidx.lifecycle.ViewModel
import com.example.autolog_20.ui.theme.data.api.RetrofitClient
import com.example.autolog_20.ui.theme.data.model.STSRecognitionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MultipartBody
import timber.log.Timber

class STSViewModel : ViewModel() {

    private val stsApi = RetrofitClient.stsApi

    private val _uiState = MutableStateFlow<STSRecognitionUiState>(STSRecognitionUiState.Idle)
    val uiState: StateFlow<STSRecognitionUiState> = _uiState.asStateFlow()

    suspend fun recognizeSTS(photoPart: MultipartBody.Part) {
        _uiState.value = STSRecognitionUiState.Loading

        try {
            Timber.d("STSViewModel: Sending request to server...")
            val response = stsApi.recognizeSTS(photoPart)
            Timber.d("STSViewModel: Response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Timber.d("STSViewModel: Response body: success=${body.success}, data=${body.data}")

                if (body.success && body.data != null) {
                    Timber.d("STSViewModel: Success: ${body.data}")
                    _uiState.value = STSRecognitionUiState.Success(body.data)
                } else {
                    Timber.e( "STSViewModel: Error from server: ${body.error}")
                    _uiState.value = STSRecognitionUiState.Error(body.error ?: "Ошибка распознавания")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Timber.e("STSViewModel: Server error: ${response.code()}, body: $errorBody")
                _uiState.value = STSRecognitionUiState.Error("Ошибка сервера: ${response.code()}")
            }
        } catch (e: Exception) {
            Timber.e( "STSViewModel: Network error")
            _uiState.value = STSRecognitionUiState.Error("Сетевая ошибка: ${e.localizedMessage}")
        }
    }

    fun reset() {
        _uiState.value = STSRecognitionUiState.Idle
    }
}