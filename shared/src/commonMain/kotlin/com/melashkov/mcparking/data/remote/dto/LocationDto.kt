package com.melashkov.mcparking.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val id: Int,
    val title: String,
    val type: Int,
    val latitude: Double,
    val longitude: Double,
    val description: String,
)
