package com.example.autolog_20.ui.theme.data.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenRefreshApi {

    @POST("api/token/refresh/")
    fun refreshToken(@Body body: Map<String, String>): Call<Map<String, Any>>
}