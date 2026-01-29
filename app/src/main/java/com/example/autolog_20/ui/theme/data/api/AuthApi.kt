package com.example.autolog_20.ui.theme.data.api

import com.example.autolog_20.ui.theme.data.model.LoginRequest
import com.example.autolog_20.ui.theme.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/login/")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    companion object {
        const val BASE_URL = "http://10.0.2.2:8000/"
    }
}