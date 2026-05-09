package com.example.autolog_20.ui.theme.data.model.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autolog_20.ui.theme.data.api.AuthApi
import com.example.autolog_20.ui.theme.data.model.AddCarFromSTSUiState
import com.example.autolog_20.ui.theme.data.model.DrivingSurvey
import com.example.autolog_20.ui.theme.data.model.STSRecognitionData
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
import java.text.SimpleDateFormat
import java.util.*

class AddCarFromSTSViewModel(
    private val authApi: AuthApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddCarFromSTSUiState>(AddCarFromSTSUiState.Input)
    val uiState: StateFlow<AddCarFromSTSUiState> = _uiState.asStateFlow()

    private var currentSTSData: STSRecognitionData? = null
    private var currentMileage: Int = 0
    private var currentYear: Int = 0
    private var currentNumber: String = ""
    private var currentColor: String = ""
    private var isCheckingPlate = false

    fun processSTSData(data: STSRecognitionData) {
        // Проверяем VIN
        if (data.vin.length != 17 || !data.vin.matches(Regex("^[A-HJ-NPR-Z0-9]{17}$"))) {
            _uiState.value = AddCarFromSTSUiState.Error("VIN должен содержать 17 символов и не содержать I, O, Q")
            return
        }

        // Проверяем марку и модель
        if (data.brand.isBlank() || data.model.isBlank()) {
            _uiState.value = AddCarFromSTSUiState.Error("Не удалось определить марку или модель автомобиля")
            return
        }

        // Проверяем номер (если распознан)
        if (data.numberPlate.isNotBlank()) {
            checkPlateAndVIN(data)
        } else {
            // Если номер не распознан, проверяем только VIN
            checkVINOnly(data)
        }
    }

    private fun checkPlateAndVIN(data: STSRecognitionData) {
        viewModelScope.launch {
            _uiState.value = AddCarFromSTSUiState.Loading

            try {
                // Сначала проверяем номер
                val plateExists = checkPlateNumber(data.numberPlate)
                if (plateExists) {
                    return@launch
                }

                // Затем проверяем VIN
                val vinCheck = checkVINExists(data.vin)
                if (vinCheck) {
                    return@launch
                }

                // Если всё ок, показываем предпросмотр
                currentSTSData = data
                _uiState.value = AddCarFromSTSUiState.Preview(data)

            } catch (e: Exception) {
                _uiState.value = AddCarFromSTSUiState.Error("Ошибка: ${e.localizedMessage}")
            }
        }
    }

    private fun checkVINOnly(data: STSRecognitionData) {
        viewModelScope.launch {
            _uiState.value = AddCarFromSTSUiState.Loading

            try {
                val vinCheck = checkVINExists(data.vin)
                if (vinCheck) {
                    return@launch
                }

                currentSTSData = data
                _uiState.value = AddCarFromSTSUiState.Preview(data)

            } catch (e: Exception) {
                _uiState.value = AddCarFromSTSUiState.Error("Ошибка: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun checkPlateNumber(number: String): Boolean {
        if (isCheckingPlate) return false
        isCheckingPlate = true

        return try {
            val response = authApi.getCarByPlate(number)
            if (response.isSuccessful && response.body() != null) {
                _uiState.value = AddCarFromSTSUiState.PlateAlreadyExistsError(
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

    private suspend fun checkVINExists(vin: String): Boolean {
        return try {
            val response = authApi.checkVinExists(vin)
            if (response.isSuccessful) {
                val vinCheck = response.body()
                if (vinCheck?.exists == true) {
                    _uiState.value = AddCarFromSTSUiState.VinAlreadyExistsError(
                        message = "Автомобиль с VIN $vin уже зарегистрирован в системе.",
                        existingCar = vinCheck.car!!
                    )
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun confirmData() {
        if (currentSTSData == null) return
        _uiState.value = AddCarFromSTSUiState.EnterMileage
    }

    fun saveContinue(mileage: Int, year: Int, number: String) {
        currentMileage = mileage
        currentYear = year
        currentNumber = number
        _uiState.value = AddCarFromSTSUiState.EnterColor
    }

    fun saveColorAndContinue(color: String) {
        currentColor = color
        val data = currentSTSData ?: return

        val vinPreview = VinPreviewData(
            vin = data.vin,
            brand = data.brand,
            model = data.model,
            fuel = "Petrol" // По умолчанию бензин, можно добавить распознавание топлива
        )

        _uiState.value = DrivingSurvey(vinPreview,
            mileage = currentMileage,
            fuelType = "Petrol",
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
                Result.failure(Exception("Ошибка добавления записи пробега: ${response.code()}"))
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
        _uiState.value = summary
    }

    fun confirmAllData() {
        _uiState.value = AddCarFromSTSUiState.Success
    }

    fun resetToInput() {
        _uiState.value = AddCarFromSTSUiState.Input
        currentSTSData = null
        currentMileage = 0
        currentYear = 0
        currentNumber = ""
        currentColor = ""
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
}
