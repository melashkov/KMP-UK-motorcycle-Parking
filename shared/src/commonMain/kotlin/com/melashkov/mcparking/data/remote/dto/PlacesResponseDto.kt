package com.melashkov.mcparking.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class PlacesResponseDto(
    val status: String,
    val results: List<PlaceDto> = emptyList(),
)
