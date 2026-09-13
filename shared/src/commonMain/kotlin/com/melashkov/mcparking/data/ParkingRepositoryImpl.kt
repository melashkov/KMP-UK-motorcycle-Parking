package com.melashkov.mcparking.data

import com.melashkov.mcparking.data.remote.ParkingRemoteDataSource
import com.melashkov.mcparking.data.remote.dto.ApiErrorResponseDto
import com.melashkov.mcparking.data.remote.dto.ParkingReportResponseDto
import com.melashkov.mcparking.data.remote.dto.toDomain
import com.melashkov.mcparking.data.remote.dto.toDto
import com.melashkov.mcparking.domain.entity.GeoBounds
import com.melashkov.mcparking.domain.entity.ParkingBay
import com.melashkov.mcparking.domain.entity.ParkingBaySubmission
import com.melashkov.mcparking.domain.interfaces.AppError
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.interfaces.ParkingRepository
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import org.koin.core.annotation.Singleton

@Singleton
class ParkingRepositoryImpl(
    private val remote: ParkingRemoteDataSource,
) : ParkingRepository {

    override suspend fun getParkingBays(
        bounds: GeoBounds,
    ): DataResult<List<ParkingBay>> {
        return try {
            val response = remote.getParkingBays(bounds)

            DataResult.Success(
                response.results.map { it.toDomain() }
            )
        } catch (e: IOException) {
            DataResult.Failure(AppError.ConnectionFailed)
        } catch (e: ClientRequestException) {
            when (e.response.status.value) {
                401 -> DataResult.Failure(AppError.Unauthorized)
                403 -> DataResult.Failure(AppError.Forbidden)
                429 -> DataResult.Failure(AppError.RateLimited)
                else -> DataResult.Failure(
                    e.response.toServerErrorOrNull() ?: AppError.Unknown,
                )
            }
        } catch (e: ServerResponseException) {
            DataResult.Failure(
                e.response.toServerErrorOrNull() ?: AppError.ServerUnavailable,
            )
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            DataResult.Failure(AppError.Unknown)
        }
    }

    override suspend fun submitParkingBay(
        submission: ParkingBaySubmission,
    ): DataResult<Unit> {
        return try {
            val response = remote.submitParkingBay(submission.toDto())
            if (response.status.equals("OK", ignoreCase = true)) {
                DataResult.Success(Unit)
            } else {
                DataResult.Failure(response.toServerErrorOrNull() ?: AppError.Unknown)
            }
        } catch (e: IOException) {
            DataResult.Failure(AppError.ConnectionFailed)
        } catch (e: ClientRequestException) {
            when (e.response.status.value) {
                401 -> DataResult.Failure(AppError.Unauthorized)
                403 -> DataResult.Failure(AppError.Forbidden)
                429 -> DataResult.Failure(AppError.RateLimited)
                else -> DataResult.Failure(
                    e.response.toServerErrorOrNull() ?: AppError.Unknown,
                )
            }
        } catch (e: ServerResponseException) {
            DataResult.Failure(
                e.response.toServerErrorOrNull() ?: AppError.ServerUnavailable,
            )
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            DataResult.Failure(AppError.Unknown)
        }
    }
}

private fun ParkingReportResponseDto.toServerErrorOrNull() =
    message?.takeIf(String::isNotBlank)?.let { AppError.ServerError(it) }

private suspend fun HttpResponse.toServerErrorOrNull(): AppError.ServerError? {
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
