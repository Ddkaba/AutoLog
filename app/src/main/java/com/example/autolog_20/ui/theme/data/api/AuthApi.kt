package com.example.autolog_20.ui.theme.data.api

import com.example.autolog_20.ui.theme.data.model.ExpenseItem
import com.example.autolog_20.ui.theme.data.model.request.AddCarToUserRequest
import com.example.autolog_20.ui.theme.data.model.request.AddMileageRequest
import com.example.autolog_20.ui.theme.data.model.request.AddRecommendationRequest
import com.example.autolog_20.ui.theme.data.model.request.CarUpdateRequest
import com.example.autolog_20.ui.theme.data.model.request.CreateCarRequest
import com.example.autolog_20.ui.theme.data.model.request.ExpenseUpdateRequest
import com.example.autolog_20.ui.theme.data.model.response.VinCheckResponse
import com.example.autolog_20.ui.theme.data.model.response.CarDetailResponse
import com.example.autolog_20.ui.theme.data.model.response.CarResponse
import com.example.autolog_20.ui.theme.data.model.response.ExpensesResponse
import com.example.autolog_20.ui.theme.data.model.request.LoginRequest
import com.example.autolog_20.ui.theme.data.model.response.LoginResponse
import com.example.autolog_20.ui.theme.data.model.response.RecommendationResponse
import com.example.autolog_20.ui.theme.data.model.request.RegisterRequest
import com.example.autolog_20.ui.theme.data.model.response.AddCarToUserResponse
import com.example.autolog_20.ui.theme.data.model.response.CarAddResponse
import com.example.autolog_20.ui.theme.data.model.response.RegisterResponse
import com.example.autolog_20.ui.theme.data.model.response.TireResponse
import com.example.autolog_20.ui.theme.data.model.response.VinInfoResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {


    @GET("api/my-cars/")
    suspend fun getMyCars(): Response<List<CarResponse>>

    @GET("api/cars/{carId}/recommendations/")
    suspend fun getRecommendations(
        @Path("carId") carId: Int
    ): Response<List<RecommendationResponse>>

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

    @GET("api/vin-info/")
    suspend fun getVinInfo(
        @Query("vin") vin: String
    ): Response<VinInfoResponse>

    @GET("api/cars/vin/{vin}/")
    suspend fun checkVinExists(
        @Path("vin") vin: String
    ): Response<VinCheckResponse>

    @POST("api/login/")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/register/")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/cars/{carId}/recommendations/")
    suspend fun addRecommendation(
        @Path("carId") carId: Int,
        @Body request: AddRecommendationRequest
    ): Response<Unit>

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

    @POST("api/cars/")
    suspend fun addCar(
        @Body carData: CreateCarRequest
    ): Response<CarAddResponse>

    @POST("api/my-cars/")
    suspend fun addCarToUser(
        @Body request: AddCarToUserRequest
    ): Response<AddCarToUserResponse>

    @POST("api/cars/{carId}/mileage/create/")
    suspend fun addMileageRecord(
        @Path("carId") carId: Int,
        @Body request: AddMileageRequest
    ): Response<Unit>

    @DELETE("api/cars/{carId}/")
    suspend fun deleteCar(
        @Path("carId") carId: Int
    ): Response<Unit>

    @DELETE("api/cars/{carId}/expenses/{expenseId}/")
    suspend fun deleteExpense(
        @Path("carId") carId: Int,
        @Path("expenseId") expenseId: Int
    ): Response<Unit>

    @PATCH("api/cars/{carId}/")
    suspend fun updateCar(
        @Path("carId") carId: Int,
        @Body request: CarUpdateRequest
    ): Response<CarDetailResponse>

    @PATCH("api/cars/{carId}/expenses/{expenseId}/")
    suspend fun updateExpense(
        @Path("carId") carId: Int,
        @Path("expenseId") expenseId: Int,
        @Body request: ExpenseUpdateRequest
    ): Response<ExpenseItem>


    companion object {
        const val BASE_URL = "http://10.0.2.2:8000/"
    }
}