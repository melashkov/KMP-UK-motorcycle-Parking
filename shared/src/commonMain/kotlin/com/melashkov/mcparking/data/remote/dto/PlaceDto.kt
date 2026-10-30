package com.melashkov.mcparking.data.remote.dto

import com.melashkov.mcparking.domain.entity.Place
import com.melashkov.mcparking.domain.entity.PlaceType
import kotlinx.serialization.Serializable

@Serializable
internal data class PlaceDto(
    val id: String,
    val name: String,
    val subtitle: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
) {
    fun toDomain(): Place = Place(
        id = id,
        name = name,
        subtitle = subtitle,
        type = PlaceType.fromString(type),
        latitude = latitude,
        longitude = longitude,
    )
}
