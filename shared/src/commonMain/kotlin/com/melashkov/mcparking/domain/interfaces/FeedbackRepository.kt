package com.melashkov.mcparking.domain.interfaces

interface FeedbackRepository {
    suspend fun send(message: String, email: String): DataResult<Unit>
}
