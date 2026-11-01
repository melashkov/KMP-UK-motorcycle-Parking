package com.melashkov.mcparking.data.remote

import com.melashkov.mcparking.data.remote.dto.ApiErrorResponseDto
import com.melashkov.mcparking.domain.interfaces.AppError
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CancellationException

internal suspend fun HttpResponse.toServerErrorOrNull(): AppError.ServerError? {
    val response = try {
        body<ApiErrorResponseDto>()
    } catch (error: CancellationException) {
        throw error
    } catch (_: Exception) {
        return null
    }

    return response.message
        ?.takeIf(String::isNotBlank)
        ?.let { AppError.ServerError(it) }
}
