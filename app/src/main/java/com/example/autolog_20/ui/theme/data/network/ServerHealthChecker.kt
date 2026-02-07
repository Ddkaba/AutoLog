package com.example.autolog_20.ui.theme.data.network

import com.example.autolog_20.ui.theme.data.api.AuthApi
import kotlinx.coroutines.withTimeoutOrNull

suspend fun AuthApi.isServerReachable(timeoutMs: Long = 5000): Boolean =
    withTimeoutOrNull(timeoutMs) {
        runCatching {
            val response = getMyCars()
            response.isSuccessful || response.code() in 400..499
        }.getOrDefault(false)
    } ?: false
