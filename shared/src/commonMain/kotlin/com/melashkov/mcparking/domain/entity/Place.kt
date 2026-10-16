package com.melashkov.mcparking.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Place(
    val id: String,
    val name: String,
    val subtitle: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
)
