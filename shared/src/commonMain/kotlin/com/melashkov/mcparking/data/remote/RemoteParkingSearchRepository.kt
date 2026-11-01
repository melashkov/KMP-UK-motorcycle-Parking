package com.melashkov.mcparking.data.remote

import com.melashkov.mcparking.data.remote.dto.ParkingResponseDto
import com.melashkov.mcparking.data.remote.dto.toDomain
import com.melashkov.mcparking.domain.entity.GeoBounds
import com.melashkov.mcparking.domain.entity.ParkingBay
import com.melashkov.mcparking.domain.interfaces.AppError
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.interfaces.ParkingSearchRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import org.koin.core.annotation.Singleton

@Singleton
class RemoteParkingSearchRepository(private val client: HttpClient) : ParkingSearchRepository {
    override suspend fun getParkingBays(
        bounds: GeoBounds,
    ): DataResult<List<ParkingBay>> {
        return try {
            val response = client.get("bounds.php") {
                parameter("north", bounds.north)
                parameter("south", bounds.south)
                parameter("east", bounds.east)
                parameter("west", bounds.west)
            }.body<ParkingResponseDto>()

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
}
