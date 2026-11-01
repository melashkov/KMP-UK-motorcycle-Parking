package com.melashkov.mcparking.domain.entity

data class Place(
    val id: String,
    val name: String,
    val subtitle: String,
    val type: PlaceType,
    val latitude: Double,
    val longitude: Double,
)
