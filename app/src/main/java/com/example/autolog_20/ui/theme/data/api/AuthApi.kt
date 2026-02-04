package com.example.autolog_20.ui.theme.data.api

import com.example.autolog_20.ui.theme.data.model.LoginRequest
import com.example.autolog_20.ui.theme.data.model.LoginResponse
import com.example.autolog_20.ui.theme.data.model.RefreshRequest
import com.example.autolog_20.ui.theme.data.model.RefreshResponse
import com.example.autolog_20.ui.theme.data.model.RegisterRequest
import com.example.autolog_20.ui.theme.data.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/login/")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/register/")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/token/refresh/")
    suspend fun refreshToken(
        @Body request: RefreshRequest
    ): retrofit2.Response<RefreshResponse>

    companion object {
        const val BASE_URL = "http://10.0.2.2:8000/"
    }
}