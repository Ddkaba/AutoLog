package com.example.autolog_20.ui.theme.data.model.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autolog_20.ui.theme.data.api.AuthApi
import com.example.autolog_20.ui.theme.data.model.ManualCarPreview
import com.example.autolog_20.ui.theme.data.model.SurveySummary
import com.example.autolog_20.ui.theme.data.model.AddCarManualUiState
import com.example.autolog_20.ui.theme.data.model.DrivingSurvey
import com.example.autolog_20.ui.theme.data.model.VinPreviewData
import com.example.autolog_20.ui.theme.data.model.request.AddCarToUserRequest
import com.example.autolog_20.ui.theme.data.model.request.AddMileageRequest
import com.example.autolog_20.ui.theme.data.model.request.AddRecommendationRequest
import com.example.autolog_20.ui.theme.data.model.request.CreateCarRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddCarManualViewModel(
    private val authApi: AuthApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddCarManualUiState>(AddCarManualUiState.Input)
    val uiState: StateFlow<AddCarManualUiState> = _uiState.asStateFlow()

    private var currentCarPreview: ManualCarPreview? = null
    private var currentMileage: Int = 0
    private var currentYear: Int = 0
    private var currentNumber: String = ""
    private var currentColor: String = ""

    fun checkCarData(vin: String, brand: String, model: String, fuelType: String) {
        val cleanVin = vin.trim().uppercase()
        val cleanBrand = brand.trim()
        val cleanModel = model.trim()

        if (cleanVin.length != 17 || !cleanVin.matches(Regex("^[A-HJ-NPR-Z0-9]{17}$"))) {
            _uiState.value = AddCarManualUiState.Error("VIN должен содержать 17 символов и не содержать I, O, Q")
            return
        }

        if (cleanBrand.isEmpty() || cleanModel.isEmpty()) {
            _uiState.value = AddCarManualUiState.Error("Марка и модель не могут быть пустыми")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddCarManualUiState.Loading

            try {
                val checkResponse = authApi.checkVinExists(cleanVin)

                if (checkResponse.isSuccessful) {
                    val vinCheck = checkResponse.body()
                    if (vinCheck?.exists == true) {
                        _uiState.value = AddCarManualUiState.Error(
                            "Автомобиль с VIN $cleanVin уже зарегистрирован в системе."
                        )
                        return@launch
                    }
                }

                currentCarPreview = ManualCarPreview(
                    vin = cleanVin,
                    brand = cleanBrand,
                    model = cleanModel,
                    fuelType = fuelType
                )

                _uiState.value = AddCarManualUiState.Preview(currentCarPreview!!)

            } catch (e: Exception) {
                _uiState.value = AddCarManualUiState.Error("Ошибка: ${e.localizedMessage}")
            }
        }
    }

    fun confirmCar() {
        if (currentCarPreview == null) return
        _uiState.value = AddCarManualUiState.EnterMileage
    }

    fun saveMileageAndContinue(mileage: Int, year: Int, number: String) {
        currentMileage = mileage
        currentYear = year
        currentNumber = number
        _uiState.value = AddCarManualUiState.EnterColor
    }

    fun saveColorAndContinue(color: String) {
        currentColor = color
        val preview = currentCarPreview ?: return

        val drivingSurvey = DrivingSurvey(
            vinData = VinPreviewData(
                vin = preview.vin,
                brand = preview.brand,
                model = preview.model,
                fuel = preview.fuelType
            ),
            mileage = currentMileage,
            fuelType = preview.fuelType,
            driveType = null,
            transmission = null,
            oilType = "Synthetic",
            climate = "Moderate",
            shortTrips = 0,
            heavyLoad = 0,
            highRpm = 0,
            dustyRoads = 0,
            cityDriving = 0,
            highwayDriving = 0,
            offroadDriving = 0,
            drivingStyle = "",
            year = currentYear,
            number = currentNumber,
            color = currentColor
        )

        _uiState.value = AddCarManualUiState.DrivingSurveyState(survey = drivingSurvey)
    }

    suspend fun addCarToBackend(carData: SurveySummary): Result<Int> {
        return try {
            val request = CreateCarRequest(
                vin = carData.vinData.vin,
                brand = carData.vinData.brand,
                model = carData.vinData.model,
                yearOfManufacture = carData.year,
                color = carData.color,
                numberPlate = carData.number
            )

            val response = authApi.addCar(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.carId)
            } else {
                Result.failure(Exception("Ошибка добавления автомобиля: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addCarToUser(carId: Int): Result<Unit> {
        return try {
            val request = AddCarToUserRequest(
                carId = carId,
                isOwner = true
            )
            val response = authApi.addCarToUser(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ошибка привязки автомобиля: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addFirstMileageRecord(carId: Int, mileage: Int): Result<Unit> {
        return try {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val request = AddMileageRequest(
                date = currentDate,
                mileage = mileage,
                route = "Первая запись авто"
            )
            val response = authApi.addMileageRecord(carId, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ошибка добавления пробега: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addRecommendations(carId: Int, recommendations: Map<String, Int>): Result<Unit> {
        return try {
            val componentNamesRu = mapOf(
                "engine_oil" to "моторное масло",
                "air_filter" to "воздушный фильтр",
                "cabin_filter" to "салонный фильтр",
                "fuel_filter" to "топливный фильтр",
                "spark_plugs" to "свечи зажигания",
                "brake_pads" to "тормозные колодки",
                "brake_fluid" to "тормозную жидкость",
                "coolant" to "антифриз",
                "transmission_oil" to "масло в коробке передач",
                "drive_oil" to "масло в раздатке",
                "timing_belt" to "ремень ГРМ",
                "shocks" to "амортизаторы",
                "bearings" to "подшипники ступиц",
                "cv_joints" to "ШРУСы"
            )

            val descriptions = mapOf(
                "engine_oil" to "Используйте рекомендованное производителем масло",
                "air_filter" to "Замена воздушного фильтра для обеспечения нормальной работы двигателя",
                "cabin_filter" to "Обеспечивает чистоту воздуха в салоне автомобиля",
                "fuel_filter" to "Защита топливной системы от загрязнений",
                "spark_plugs" to "Обеспечивают стабильное зажигание топливной смеси",
                "brake_pads" to "Обеспечение эффективного торможения",
                "brake_fluid" to "Поддержание тормозной системы в рабочем состоянии",
                "coolant" to "Защита двигателя от перегрева и коррозии",
                "transmission_oil" to "Обеспечение плавного переключения передач",
                "drive_oil" to "Смазка механизмов раздаточной коробки",
                "timing_belt" to "Своевременная замена предотвращает обрыв и повреждение двигателя",
                "shocks" to "Обеспечивают комфорт и управляемость автомобиля",
                "bearings" to "Обеспечивают плавное вращение колес",
                "cv_joints" to "Обеспечивают передачу крутящего момента на колеса"
            )

            var hasError = false
            val errors = mutableListOf<String>()

            for ((component, mileage) in recommendations) {
                val serviceType = "Замена ${componentNamesRu[component] ?: component}"
                val description = descriptions[component] ?: "Регулярное техническое обслуживание"

                val request = AddRecommendationRequest(
                    carId = carId,
                    serviceType = serviceType,
                    recommendedMileage = mileage,
                    description = description
                )

                val response = authApi.addRecommendation(carId, request)
                if (!response.isSuccessful) {
                    hasError = true
                    errors.add("Ошибка добавления рекомендации для ${componentNamesRu[component]}: ${response.code()}")
                }
            }

            if (hasError) {
                Result.failure(Exception(errors.joinToString("\n")))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun addCompleteCarWithData(
        surveySummary: SurveySummary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val carResult = addCarToBackend(surveySummary)
                if (carResult.isFailure) {
                    onError(carResult.exceptionOrNull()?.message ?: "Ошибка добавления автомобиля")
                    return@launch
                }

                val carId = carResult.getOrNull()!!

                val userCarResult = addCarToUser(carId)
                if (userCarResult.isFailure) {
                    onError("Автомобиль создан, но ошибка привязки к пользователю")
                    return@launch
                }

                val mileageResult = addFirstMileageRecord(carId, surveySummary.mileage)
                if (mileageResult.isFailure) {
                    onError("Автомобиль добавлен, но ошибка сохранения пробега")
                    return@launch
                }

                val recommendationsResult = addRecommendations(carId, surveySummary.maintenanceIntervals)
                if (recommendationsResult.isFailure) {
                    onError("Автомобиль добавлен, но ошибка сохранения рекомендаций")
                    return@launch
                }

                onSuccess()

            } catch (e: Exception) {
                onError(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    fun goToSummary(summary: SurveySummary) {
        _uiState.value = AddCarManualUiState.SurveySummaryState(summary = summary)
    }

    fun confirmAllData() {
        _uiState.value = AddCarManualUiState.Success
    }

    fun resetToInput() {
        _uiState.value = AddCarManualUiState.Input
        currentCarPreview = null
    }
}