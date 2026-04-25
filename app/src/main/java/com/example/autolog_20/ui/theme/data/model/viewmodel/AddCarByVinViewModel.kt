package com.example.autolog_20.ui.theme.data.model.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autolog_20.ui.theme.data.api.AuthApi
import com.example.autolog_20.ui.theme.data.model.AddCarByVinUiState
import com.example.autolog_20.ui.theme.data.model.DrivingSurvey
import com.example.autolog_20.ui.theme.data.model.SurveySummary
import com.example.autolog_20.ui.theme.data.model.VinPreviewData
import com.example.autolog_20.ui.theme.data.model.request.AddCarToUserRequest
import com.example.autolog_20.ui.theme.data.model.request.AddMileageRequest
import com.example.autolog_20.ui.theme.data.model.request.AddRecommendationRequest
import com.example.autolog_20.ui.theme.data.model.request.CreateCarRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddCarByVinViewModel(
    private val authApi: AuthApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddCarByVinUiState>(AddCarByVinUiState.Input)
    val uiState: StateFlow<AddCarByVinUiState> = _uiState.asStateFlow()

    private var currentVinPreview: VinPreviewData? = null
    private var currentMileage: Int = 0
    private var currentYear: Int = 0
    private var currentNumber: String = ""
    private var currentColor: String = ""
    private var isCheckingPlate = false

    fun checkVin(vin: String) {
        val cleanVin = vin.trim().uppercase()

        if (cleanVin.length != 17 || !cleanVin.matches(Regex("^[A-HJ-NPR-Z0-9]{17}$"))) {
            _uiState.value = AddCarByVinUiState.Error("VIN должен содержать 17 символов и не содержать I, O, Q")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddCarByVinUiState.Loading

            try {
                val checkResponse = authApi.checkVinExists(cleanVin)

                if (checkResponse.isSuccessful) {
                    val vinCheck = checkResponse.body()

                    if (vinCheck?.exists == true) {
                        _uiState.value = AddCarByVinUiState.VinAlreadyExistsError(
                            message = "Автомобиль с VIN $cleanVin уже зарегистрирован в системе.",
                            existingCar = vinCheck.car!!
                        )
                        return@launch
                    }
                }

                val response = authApi.getVinInfo(cleanVin)

                if (response.isSuccessful) {
                    val vinInfo = response.body()

                    if (vinInfo == null || vinInfo.status != "OK") {
                        _uiState.value = AddCarByVinUiState.Error("Сервер не смог обработать VIN")
                        return@launch
                    }

                    val reportData = vinInfo.reports.firstOrNull()?.data

                    if (reportData == null) {
                        _uiState.value = AddCarByVinUiState.Error("Не удалось получить данные об автомобиле")
                        return@launch
                    }

                    val brand = reportData.brand?.trim()
                    val model = reportData.model?.trim()
                    val fuelType = reportData.fuel?.trim() ?: "Petrol"

                    if (brand.isNullOrBlank() || model.isNullOrBlank()) {
                        _uiState.value = AddCarByVinUiState.Error(
                            "Не удалось определить марку или модель по VIN.\n" +
                                    "Проверьте правильность введённого VIN."
                        )
                        return@launch
                    }

                    currentVinPreview = VinPreviewData(
                        vin = cleanVin,
                        brand = brand,
                        model = model,
                        fuel = fuelType
                    )

                    _uiState.value = AddCarByVinUiState.Preview(currentVinPreview!!)

                } else {
                    _uiState.value = AddCarByVinUiState.Error("Ошибка сервера: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = AddCarByVinUiState.Error("Ошибка сети: ${e.localizedMessage}")
            }
        }
    }

    fun confirmVin() {
        if (currentVinPreview == null) return
        _uiState.value = AddCarByVinUiState.EnterMileage
    }

    fun saveContinue(mileage: Int, year: Int, number: String) {
        currentMileage = mileage
        currentYear = year
        currentNumber = number
        _uiState.value = AddCarByVinUiState.EnterColor
    }

    fun saveColorAndContinue(color: String) {
        currentColor = color
        val preview = currentVinPreview ?: return
        _uiState.value = DrivingSurvey(
            vinData = preview,
            mileage = currentMileage,
            fuelType = preview.fuel,
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
    }

    suspend fun checkPlateNumber(number: String): Boolean {
        if (isCheckingPlate) return false
        isCheckingPlate = true

        return try {
            val response = authApi.getCarByPlate(number)
            if (response.isSuccessful && response.body() != null) {
                _uiState.value = AddCarByVinUiState.PlateAlreadyExistsError(
                    message = "Автомобиль с номером $number уже зарегистрирован в системе.",
                    existingCar = response.body()!!
                )
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        } finally {
            isCheckingPlate = false
        }
    }

    fun checkPlateAndContinue(number: String, mileage: Int, year: Int, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val exists = checkPlateNumber(number)
                if (!exists) {
                    saveContinue(mileage, year, number)
                } else {
                    onError("Автомобиль с таким номером уже зарегистрирован")
                }
            } catch (e: Exception) {
                onError("Ошибка проверки номера: ${e.message}")
            }
        }
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
                Result.failure(Exception("Ошибка добавления автомобиля пользователю: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addFirstMileageRecord(carId: Int, mileage: Int): Result<Unit> {
        return try {
            val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())

            val request = AddMileageRequest(
                date = currentDate,
                mileage = mileage,
                route = "Первая запись авто"
            )
            val response = authApi.addMileageRecord(carId, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ошибка добавления записи пробега: ${response.code()}"))
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
                println("DEBUG: Car created with ID: $carId")

                val userCarResult = addCarToUser(carId)
                if (userCarResult.isFailure) {
                    val error = userCarResult.exceptionOrNull()
                    println("DEBUG: Failed to add car to user: ${error?.message}")
                    onError("Автомобиль создан, но ошибка привязки к пользователю: ${error?.message}")
                    return@launch
                }

                println("DEBUG: Car added to user successfully")

                val mileageResult = addFirstMileageRecord(carId, surveySummary.mileage)
                if (mileageResult.isFailure) {
                    val error = mileageResult.exceptionOrNull()
                    println("DEBUG: Failed to add mileage record: ${error?.message}")
                    onError("Автомобиль добавлен, но ошибка сохранения пробега: ${error?.message}")
                    return@launch
                }

                println("DEBUG: Mileage record added successfully")

                val recommendationsResult = addRecommendations(carId, surveySummary.maintenanceIntervals)
                if (recommendationsResult.isFailure) {
                    val error = recommendationsResult.exceptionOrNull()
                    println("DEBUG: Failed to add recommendations: ${error?.message}")
                    onError("Автомобиль добавлен, но ошибка сохранения рекомендаций: ${error?.message}")
                    return@launch
                }

                println("DEBUG: All recommendations added successfully")
                println("DEBUG: All operations completed successfully")
                onSuccess()

            } catch (e: Exception) {
                println("DEBUG: Exception in addCompleteCarWithData: ${e.message}")
                onError(e.message ?: "Неизвестная ошибка")
            }
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

    fun resetToInput() {
        _uiState.value = AddCarByVinUiState.Input
        currentVinPreview = null
        currentMileage = 0
        currentYear = 0
        currentNumber = ""
        currentColor = ""
    }

    fun goToSummary(summary: SurveySummary) {
        _uiState.value = summary
    }

    fun confirmAllData() {
        _uiState.value = AddCarByVinUiState.Success
    }
}





