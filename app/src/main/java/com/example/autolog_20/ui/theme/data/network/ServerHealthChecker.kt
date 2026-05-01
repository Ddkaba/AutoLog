package com.example.autolog_20.ui.theme.data.network

import com.example.autolog_20.ui.theme.data.api.AuthApi
import kotlinx.coroutines.withTimeoutOrNull

suspend fun AuthApi.isServerReachable(timeoutMs: Long = 8000): Boolean {
    return try {
        val response = withTimeoutOrNull(timeoutMs) {
            getMyCars()
        }
        response?.isSuccessful == true || response?.code() == 401
    } catch (e: Exception) {
        false
    }
}