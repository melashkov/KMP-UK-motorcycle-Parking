package com.melashkov.mcparking.domain.interfaces

import com.melashkov.mcparking.domain.entity.GeoBounds
import com.melashkov.mcparking.domain.entity.ParkingBay
import com.melashkov.mcparking.domain.entity.ParkingBaySubmission

interface ParkingRepository {
    suspend fun getParkingBays(
        bounds: GeoBounds,
    ): DataResult<List<ParkingBay>>

    suspend fun submitParkingBay(
        submission: ParkingBaySubmission,
    ): DataResult<Unit>
}

sealed interface DataResult<out T> {
    data class Success<T>(
        val value: T,
    ) : DataResult<T>

    data class Failure(
        val error: DataError,
    ) : DataResult<Nothing>
}

sealed interface DataError {
    data object Offline : DataError
    data object Unauthorized : DataError
    data object Forbidden : DataError
    data object RateLimited : DataError
    data object ServerUnavailable : DataError
    data object Unknown : DataError
}
