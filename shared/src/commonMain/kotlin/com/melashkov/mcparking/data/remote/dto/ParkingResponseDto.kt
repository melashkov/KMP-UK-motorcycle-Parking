package com.melashkov.mcparking.data.remote.dto

import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.ParkingBay
import com.melashkov.mcparking.domain.entity.ParkingType
import kotlinx.serialization.Serializable

@Serializable
data class ParkingResponseDto(
    val status: String,
    val count: Int,
    val limit: Int,
    val bounds: GeoBoundsDto,
    val results: List<LocationDto>,
)

fun LocationDto.toDomain() =
    ParkingBay(
        id = id.toString(),
        title = title,
        type = ParkingType.fromInt(type),
        position = GeoCoordinate(
            latitude = latitude,
            longitude = longitude,
        ),
        description = description,
    )
