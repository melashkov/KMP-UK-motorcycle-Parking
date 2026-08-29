package com.melashkov.mcparking.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeoBoundsDto(
    val north: Double,
    val south: Double,
    val east: Double,
    val west: Double,
)