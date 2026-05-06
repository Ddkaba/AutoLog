package com.example.autolog_20.ui.theme.data.model

data class RouteInfo(
    val distance: Double,  // метры
    val duration: Double,  // секунды
    val polyline: List<List<Double>>,  // список точек для отрисовки
    val maneuvers: List<RouteManeuver>
)