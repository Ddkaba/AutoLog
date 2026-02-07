package com.example.autolog_20.ui.theme.data.network

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
    val isConnected: Flow<Boolean>
    fun isCurrentlyConnected(): Boolean
}