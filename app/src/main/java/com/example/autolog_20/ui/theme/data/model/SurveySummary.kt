package com.example.autolog_20.ui.theme.data.model

data class SurveySummary(
    val vinData: VinPreviewData,
    val mileage: Int,
    val fuelType: String,
    val driveType: String,
    val transmission: String,
    val oilType: String,
    val climate: String,
    val shortTrips: Int,
    val heavyLoad: Int,
    val highRpm: Int,
    val dustyRoads: Int,
    val cityDriving: Int,
    val highwayDriving: Int,
    val offroadDriving: Int,
    val drivingStyle: String,
    val year: Int = 0,
    val number: String = "",
    val color: String = "",
    val maintenanceIntervals: Map<String, Int> = emptyMap()
) : AddCarByVinUiState