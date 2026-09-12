package com.melashkov.mcparking.domain.entity

data class ParkingBaySubmission(
    val parentId: String?,
    val title: String,
    val description: String,
    val type: ParkingType,
    val position: GeoCoordinate,
)
