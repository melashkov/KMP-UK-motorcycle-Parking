package com.melashkov.mcparking.domain.entity

data class MapViewport(
    val bounds: GeoBounds,
    val zoom: Double,
    val center: GeoCoordinate
)