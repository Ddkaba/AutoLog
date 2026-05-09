package com.example.autolog_20.ui.theme.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

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
            .addInterceptor(AuthInterceptor(tokenRefreshApi))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private val okHttpClientSts: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenRefreshApi))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(240, TimeUnit.SECONDS)   // 2 минуты на подключение
            .readTimeout(240, TimeUnit.SECONDS)      // 2 минуты на чтение
            .writeTimeout(240, TimeUnit.SECONDS)     // 2 минуты на запись
            .callTimeout(240, TimeUnit.SECONDS)      // 2 минуты на весь вызов
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

    val stsApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(AuthApi.BASE_URL)
            .client(okHttpClientSts)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}