package com.melashkov.mcparking.domain.entity

data class ParkingBay(
    val id: String,
    val title: String,
    val type: ParkingType,
    val sector: String,
    val position: GeoCoordinate,
    val description: String,
)
