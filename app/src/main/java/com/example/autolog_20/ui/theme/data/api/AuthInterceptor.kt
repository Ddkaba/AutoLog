package com.example.autolog_20.ui.theme.data.api

import com.example.autolog_20.ui.theme.data.locale.TokenManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response as OkHttpResponse
import retrofit2.Response as RetrofitResponse
import java.io.IOException

class AuthInterceptor(
    private val tokenRefreshApi: TokenRefreshApi
) : Interceptor {

    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val BEARER = "Bearer "
        private val refreshLock = Any()
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): OkHttpResponse {
        var request: Request = chain.request()

        val accessToken = TokenManager.getAccessToken()
        if (!accessToken.isNullOrBlank()) {
            request = request.newBuilder()
                .header(HEADER_AUTHORIZATION, BEARER + accessToken)
                .build()
        }

        var response: OkHttpResponse = chain.proceed(request)

        if (response.code == 401) {
            synchronized(refreshLock) {
                val currentAccess = TokenManager.getAccessToken()
                if (currentAccess != accessToken || currentAccess == null) {
                    response.close()
                    return chain.proceed(
                        request.newBuilder()
                            .header(HEADER_AUTHORIZATION, BEARER + currentAccess)
                            .build()
                    )
                }

                val refreshSuccess = refreshToken()

                if (refreshSuccess) {
                    response.close()

                    val newAccess = TokenManager.getAccessToken()
                    if (newAccess != null) {
                        return chain.proceed(
                            request.newBuilder()
                                .header(HEADER_AUTHORIZATION, BEARER + newAccess)
                                .build()
                        )
                    }
                }
            }
            TokenManager.clearTokens()
            throw RefreshTokenFailedException("Не удалось обновить токен. Пожалуйста, войдите заново.")
        }

        return response
    }

    private fun refreshToken(): Boolean {
        val refreshToken = TokenManager.getRefreshToken() ?: return false

        try {
            val requestBody = mapOf("refresh" to refreshToken)
            val retrofitCall = tokenRefreshApi.refreshToken(requestBody)
            val retrofitResponse: RetrofitResponse<Map<String, Any>> = retrofitCall.execute()

            if (retrofitResponse.isSuccessful) {
                retrofitResponse.body()?.let { body ->
                    val newAccess = body["access"] as? String ?: return false

                    var newRefresh: String? = null
                    val setCookie = retrofitResponse.headers()["Set-Cookie"]

                    if (!setCookie.isNullOrBlank()) {
                        val cookieParts = setCookie.split(";")
                        for (part in cookieParts) {
                            val trimmed = part.trim()
                            if (trimmed.startsWith("refresh_token=", ignoreCase = true)) {
                                newRefresh = trimmed.substringAfter("=").trim()
                                break
                            }
                        }
                    }

                    TokenManager.saveTokens(
                        access = newAccess,
                        refresh = newRefresh ?: refreshToken,
                        userId = TokenManager.getUserId(),
                        username = TokenManager.getUsername() ?: ""
                    )

                    return true
                }
            } else {
                val statusCode = retrofitResponse.code()

                if (statusCode in listOf(400, 401, 403)) {
                    TokenManager.clearTokens()
                }

                retrofitResponse.errorBody()?.string()?.let { error ->
                    println("Refresh token error: $error")
                }
            }

            return false

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}