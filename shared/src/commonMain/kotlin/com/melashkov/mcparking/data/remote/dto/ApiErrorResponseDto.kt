package com.melashkov.mcparking.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponseDto(
    val status: String? = null,
    val message: String? = null,
)
