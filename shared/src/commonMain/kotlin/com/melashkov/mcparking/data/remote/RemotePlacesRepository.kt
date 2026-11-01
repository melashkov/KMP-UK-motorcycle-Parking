package com.melashkov.mcparking.data.remote

import com.melashkov.mcparking.data.remote.dto.PlacesResponseDto
import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.Place
import com.melashkov.mcparking.domain.interfaces.AppError
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.interfaces.PlacesRepository
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
class RemotePlacesRepository(private val client: HttpClient) : PlacesRepository {
    override suspend fun search(query: String, near: GeoCoordinate?): DataResult<List<Place>> = try {
        val response = client.get("places.php") {
            parameter("q", query)
            near?.let {
                parameter("latitude", it.latitude)
                parameter("longitude", it.longitude)
            }
        }.body<PlacesResponseDto>()
        if (response.status == "OK" || response.status == "NO_RESULTS") {
            DataResult.Success(response.results.map { it.toDomain() }.filter {
                it.latitude.isFinite() && it.longitude.isFinite() &&
                    it.latitude in -90.0..90.0 && it.longitude in -180.0..180.0
            })
        } else DataResult.Failure(AppError.ServerUnavailable)
    } catch (error: CancellationException) {
        throw error
    } catch (_: IOException) {
        DataResult.Failure(AppError.ConnectionFailed)
    } catch (error: ClientRequestException) {
        DataResult.Failure(if (error.response.status.value == 429) AppError.RateLimited else AppError.Unknown)
    } catch (_: ServerResponseException) {
        DataResult.Failure(AppError.ServerUnavailable)
    } catch (_: Exception) {
        DataResult.Failure(AppError.Unknown)
    }
}
