package com.example.autolog_20.ui.theme.data.model.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autolog_20.ui.theme.data.model.DrivingSurvey
import com.example.autolog_20.ui.theme.data.model.SurveySummary
import com.example.autolog_20.ui.theme.data.model.VinPreviewData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DrivingSurveyViewModel : ViewModel() {

    private val _surveyState = MutableStateFlow<DrivingSurvey?>(null)
    val surveyState: StateFlow<DrivingSurvey?> = _surveyState.asStateFlow()

    fun startSurvey(vinData: VinPreviewData, mileage: Int,  year: Int = 0, number: String = "", color: String = "") {
        _surveyState.value = DrivingSurvey(
            vinData = vinData,
            mileage = mileage,
            driveType = null,
            transmission = null,
            fuelType = vinData.fuel,
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
            year = year,
            number = number,
            color = color
        )
    }

    private fun update(block: (DrivingSurvey) -> DrivingSurvey) {
        val current = _surveyState.value ?: return
        _surveyState.value = block(current)
    }

    fun updateCityDriving(level: Int) {
        update { it.copy(cityDriving = level) }
    }

    fun updateHighwayDriving(level: Int) {
        update { it.copy(highwayDriving = level) }
    }

    fun updateOffroadDriving(level: Int) {
        update { it.copy(offroadDriving = level) }
    }

    fun updateDrivingStyle(style: String) {
        update { it.copy(drivingStyle = style) }
    }

    fun setDriveType(driveType: String) {
        update { it.copy(driveType = driveType) }
    }

    fun setTransmission(transmission: String) {
        update { it.copy(transmission = transmission) }
    }

    fun updateOilType(oilType: String) {
        update { it.copy(oilType = oilType) }
    }

    fun updateClimate(climate: String) {
        update { it.copy(climate = climate) }
    }

    fun updateShortTrips(value: Int) {
        update { it.copy(shortTrips = value) }
    }

    fun updateHeavyLoad(value: Int) {
        update { it.copy(heavyLoad = value) }
    }

    fun updateHighRpm(value: Int) {
        update { it.copy(highRpm = value) }
    }

    fun updateDustyRoads(value: Int) {
        update { it.copy(dustyRoads = value) }
    }

    fun buildSummary(): SurveySummary? {
        val current = _surveyState.value ?: return null
        if (current.driveType == null || current.transmission == null) return null

        return SurveySummary(
            vinData = current.vinData,
            mileage = current.mileage,
            fuelType = current.fuelType,
            driveType = current.driveType,
            transmission = current.transmission,
            cityDriving = current.cityDriving,
            highwayDriving = current.highwayDriving,
            offroadDriving = current.offroadDriving,
            drivingStyle = current.drivingStyle,
            climate = current.climate,
            oilType = current.oilType,
            shortTrips = current.shortTrips,
            heavyLoad = current.heavyLoad,
            highRpm = current.highRpm,
            dustyRoads = current.dustyRoads,
            maintenanceIntervals = calculatePersonalizedMaintenance(current),
            year = current.year,
            number = current.number,
            color = current.color
        )
    }

    private fun calculatePersonalizedMaintenance(survey: DrivingSurvey): Map<String, Int> {

        val climateCoeff = when (survey.climate) {
            "Hot" -> 0.9
            "Cold" -> 0.9
            "Extreme" -> 0.85
            else -> 1.0
        }

        val shortTripsCoeff = when (survey.shortTrips) {
            1 -> 1.0   // Редко
            2 -> 0.85  // Иногда
            3 -> 0.7   // Часто
            else -> 1.0
        }

        val heavyLoadCoeff = when (survey.heavyLoad) {
            1 -> 1.0   // Никогда
            2 -> 0.9   // Редко
            3 -> 0.8   // Иногда
            4 -> 0.7   // Часто
            else -> 1.0
        }

        val highRpmCoeff = when (survey.highRpm) {
            1 -> 1.0   // Редко
            2 -> 0.85  // Иногда
            3 -> 0.7   // Часто
            else -> 1.0
        }

        val dustyRoadsCoeff = when (survey.dustyRoads) {
            1 -> 1.0   // Нет
            2 -> 0.85  // Иногда
            3 -> 0.7   // Да
            else -> 1.0
        }

        val drivingStyleCoeff = when (survey.drivingStyle) {
            "calm" -> 1.1
            "normal" -> 1.0
            "dynamic" -> 0.9
            "aggressive" -> 0.7
            else -> 1.0
        }

        val cityDrivingCoeff = when (survey.cityDriving) {
            1 -> 1.0   // Редко
            2 -> 0.9   // Иногда
            3 -> 0.8   // Часто
            4 -> 0.7   // Почти всегда
            else -> 1.0
        }

        val highwayDrivingCoeff = when (survey.highwayDriving) {
            1 -> 1.0   // Редко
            2 -> 1.1   // Иногда
            3 -> 1.2   // Часто
            4 -> 1.3   // Очень часто
            else -> 1.0
        }

        val offroadDrivingCoeff = when (survey.offroadDriving) {
            1 -> 1.0   // Почти никогда
            2 -> 0.9   // Редко
            3 -> 0.75  // Иногда
            4 -> 0.6   // Часто
            else -> 1.0
        }

        val baseIntervals = mutableMapOf(
            "engine_oil" to if (survey.fuelType == "Diesel") 7000 else 10000,
            "air_filter" to 15000,
            "cabin_filter" to 15000,
            "fuel_filter" to if (survey.fuelType == "Diesel") 20000 else 30000,
            "spark_plugs" to if (survey.fuelType == "Diesel") 60000 else 30000,
            "brake_pads" to 40000,
            "brake_fluid" to 40000,
            "coolant" to 60000,
            "transmission_oil" to if (survey.transmission == "AT") 50000 else 60000,
            "timing_belt" to 60000,
            "drive_oil" to 40000,
            "shocks" to 80000,
            "bearings" to 100000,
            "cv_joints" to 100000
        )

        val oilTypeCoeff = when (survey.oilType) {
            "Synthetic" -> 1.0
            "SemiSynthetic" -> 0.7
            "Mineral" -> 0.5
            else -> 1.0
        }
        baseIntervals["engine_oil"] = (baseIntervals["engine_oil"]!! * oilTypeCoeff).toInt()

        val calculatedIntervals = mutableMapOf<String, Int>()

        calculatedIntervals["engine_oil"] = (
                baseIntervals["engine_oil"]!! *
                        cityDrivingCoeff *
                        highRpmCoeff *
                        shortTripsCoeff *
                        climateCoeff
                ).toInt()

        calculatedIntervals["air_filter"] = (
                baseIntervals["air_filter"]!! *
                        dustyRoadsCoeff *
                        offroadDrivingCoeff *
                        0.8
                ).toInt()

        calculatedIntervals["cabin_filter"] = (
                baseIntervals["cabin_filter"]!! *
                        cityDrivingCoeff *
                        dustyRoadsCoeff
                ).toInt()

        calculatedIntervals["fuel_filter"] = (
                baseIntervals["fuel_filter"]!! *
                        dustyRoadsCoeff *
                        offroadDrivingCoeff *
                        0.9
                ).toInt()

        calculatedIntervals["spark_plugs"] = (
                baseIntervals["spark_plugs"]!! *
                        highRpmCoeff *
                        shortTripsCoeff *
                        climateCoeff
                ).toInt()

        calculatedIntervals["brake_pads"] = (
                baseIntervals["brake_pads"]!! *
                        drivingStyleCoeff *
                        cityDrivingCoeff *
                        heavyLoadCoeff
                ).toInt()

        calculatedIntervals["brake_fluid"] = (
                baseIntervals["brake_fluid"]!! *
                        cityDrivingCoeff *
                        climateCoeff *
                        0.9
                ).toInt()

        calculatedIntervals["coolant"] = (
                baseIntervals["coolant"]!! *
                        climateCoeff *
                        highRpmCoeff
                ).toInt()

        calculatedIntervals["transmission_oil"] = (
                baseIntervals["transmission_oil"]!! *
                        if (survey.transmission == "AT") cityDrivingCoeff * 0.9 else offroadDrivingCoeff * 0.9
                ).toInt()

        calculatedIntervals["drive_oil"] = (
                baseIntervals["drive_oil"]!! *
                        offroadDrivingCoeff *
                        heavyLoadCoeff *
                        0.9
                ).toInt()

        calculatedIntervals["timing_belt"] = (
                baseIntervals["timing_belt"]!! *
                        highRpmCoeff *
                        climateCoeff
                ).toInt()

        calculatedIntervals["shocks"] = (
                baseIntervals["shocks"]!! *
                        offroadDrivingCoeff *
                        heavyLoadCoeff *
                        0.8
                ).toInt()

        calculatedIntervals["bearings"] = (
                baseIntervals["bearings"]!! *
                        offroadDrivingCoeff *
                        dustyRoadsCoeff *
                        0.85
                ).toInt()

        calculatedIntervals["cv_joints"] = (
                baseIntervals["cv_joints"]!! *
                        offroadDrivingCoeff *
                        drivingStyleCoeff *
                        0.8
                ).toInt()

        return calculatedIntervals
    }

    fun finishSurvey(addCarByVinViewModel: AddCarByVinViewModel? = null, onFinish: ((SurveySummary) -> Unit)? = null) {
        val summary = buildSummary()
        if (summary != null) {
            if (onFinish != null) {
                onFinish(summary)
            } else {
                addCarByVinViewModel?.goToSummary(summary)
            }
        }
    }

}