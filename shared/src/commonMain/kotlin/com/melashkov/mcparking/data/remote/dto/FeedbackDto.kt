package com.melashkov.mcparking.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class FeedbackRequestDto(
    val message: String,
    val email: String,
)

@Serializable
internal data class FeedbackResponseDto(
    val status: String,
)
