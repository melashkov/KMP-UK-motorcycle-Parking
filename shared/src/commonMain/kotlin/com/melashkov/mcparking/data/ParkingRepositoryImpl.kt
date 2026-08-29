package com.melashkov.mcparking.data

import com.melashkov.mcparking.data.remote.ParkingRemoteDataSource
import com.melashkov.mcparking.data.remote.dto.toDomain
import com.melashkov.mcparking.domain.entity.GeoBounds
import com.melashkov.mcparking.domain.entity.ParkingBay
import com.melashkov.mcparking.domain.interfaces.DataError
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.interfaces.ParkingRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
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
            DataResult.Failure(DataError.Offline)
        } catch (e: ClientRequestException) {
            when (e.response.status.value) {
                401 -> DataResult.Failure(DataError.Unauthorized)
                403 -> DataResult.Failure(DataError.Forbidden)
                429 -> DataResult.Failure(DataError.RateLimited)
                else -> DataResult.Failure(DataError.Unknown)
            }
        } catch (e: ServerResponseException) {
            DataResult.Failure(DataError.ServerUnavailable)
        }
    }
}