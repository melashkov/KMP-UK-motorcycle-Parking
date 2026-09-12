package com.melashkov.mcparking.data.remote.dto

import com.melashkov.mcparking.domain.entity.ParkingBaySubmission
import kotlinx.serialization.Serializable

@Serializable
data class ParkingReportRequestDto(
    val title: String,
    val description: String,
    val type: Int,
    val latitude: Double,
    val longitude: Double,
    val parentId: String? = null,
)

@Serializable
data class ParkingReportResponseDto(
    val status: String,
)

fun ParkingBaySubmission.toDto() =
    ParkingReportRequestDto(
        title = title,
        description = description,
        type = type.value,
        latitude = position.latitude,
        longitude = position.longitude,
        parentId = parentId,
    )
