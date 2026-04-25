package com.example.autolog_20.ui.theme.data.model

data class DrivingSurvey(
    val vinData: VinPreviewData,
    val mileage: Int,
    val driveType: String?,      // FWD, RWD, AWD
    val transmission: String?,   // MT, AT, CVT, AMT
    val fuelType: String = "",   // из VIN
    val oilType: String = "", // Тип масла
    val climate: String = "",  // Климат
    val shortTrips: Int = 0,
    val heavyLoad: Int = 0,
    val highRpm: Int = 0,
    val dustyRoads: Int = 0,
    val cityDriving: Int = 0,
    val highwayDriving: Int = 0,
    val offroadDriving: Int = 0,
    val drivingStyle: String = "",
    val year: Int = 0,
    val number: String = "",
    val color: String = ""
) : AddCarByVinUiState





