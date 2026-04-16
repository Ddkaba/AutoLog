package com.example.autolog_20.ui.theme.data.api

import com.example.autolog_20.ui.theme.data.model.CarDetailResponse
import com.example.autolog_20.ui.theme.data.model.CarResponse
import com.example.autolog_20.ui.theme.data.model.ExpensesResponse
import com.example.autolog_20.ui.theme.data.model.LoginRequest
import com.example.autolog_20.ui.theme.data.model.LoginResponse
import com.example.autolog_20.ui.theme.data.model.RecommendationRequest
import com.example.autolog_20.ui.theme.data.model.RecommendationResponse
import com.example.autolog_20.ui.theme.data.model.RefreshRequest
import com.example.autolog_20.ui.theme.data.model.RefreshResponse
import com.example.autolog_20.ui.theme.data.model.RegisterRequest
import com.example.autolog_20.ui.theme.data.model.RegisterResponse
import com.example.autolog_20.ui.theme.data.model.TireResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("api/my-cars/")
    suspend fun getMyCars(): Response<List<CarResponse>>

    @GET("api/cars/{carId}/recommendations/")
    suspend fun getRecommendations(
        @Path("carId") carId: Int
    ): Response<List<RecommendationResponse>>

    @POST("api/cars/{carId}/recommendations/")
    suspend fun addRecommendation(
        @Path("carId") carId: Int,
        @Body request: RecommendationRequest
    ): Response<Unit>

    @GET("api/tires/recommend/")
    suspend fun getTireRecommendation(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("current_tires") currentTires: String
    ): Response<TireResponse>

    @GET("api/cars/plate/{number_plate}/")
    suspend fun getCarByPlate(
        @Path("number_plate") numberPlate: String
    ): Response<CarDetailResponse>

    @GET("api/cars/{carId}/expenses/")
    suspend fun getExpenses(
        @Path("carId") carId: Int,
        @Query("period") period: String? = null,           // all, week, month, year, custom
        @Query("from") from: String? = null,               // YYYY-MM-DD
        @Query("to") to: String? = null,
        @Query("category") category: List<String> = emptyList()
    ): Response<ExpensesResponse>


    @Multipart
    @POST("api/cars/{carId}/expenses/create/")
    suspend fun addExpenseWithReceipt(
        @Path("carId") carId: Int,
        @Part("category") category: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part("date") date: RequestBody,
        @Part("description") description: RequestBody? = null,
        @Part("category_id") categoryId: RequestBody,
        @Part receipt_photo: MultipartBody.Part? = null
    ): Response<Unit>


    companion object {
        const val BASE_URL = "http://10.0.2.2:8000/"
    }
}