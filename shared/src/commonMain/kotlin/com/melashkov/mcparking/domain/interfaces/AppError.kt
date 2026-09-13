package com.melashkov.mcparking.domain.interfaces

/**
 * A failure category that can safely be communicated to the user.
 *
 * The UI maps each category to localized resources and can add feature-specific context. A
 * [ServerError] carries the user-safe message returned by the API.
 */
sealed interface AppError {
    data object ConnectionFailed : AppError
    data object Unauthorized : AppError
    data object Forbidden : AppError
    data object RateLimited : AppError
    data object ServerUnavailable : AppError
    data class ServerError(val message: String) : AppError
    data object Unknown : AppError
}
