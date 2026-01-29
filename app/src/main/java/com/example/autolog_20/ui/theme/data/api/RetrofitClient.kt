package com.example.autolog_20.ui.theme.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClientNoAuth: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val refreshRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(AuthApi.BASE_URL)
            .client(okHttpClientNoAuth)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val tokenRefreshApi: TokenRefreshApi by lazy {
        refreshRetrofit.create(TokenRefreshApi::class.java)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenRefreshApi))   // ← передаём нужный параметр
            .addInterceptor(loggingInterceptor)
            .build()
    }

    val api: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(AuthApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}