package com.melashkov.mcparking.domain.interfaces

sealed interface DataResult<out T> {
    data class Success<T>(
        val value: T,
    ) : DataResult<T>

    data class Failure(
        val error: AppError,
    ) : DataResult<Nothing>
}
