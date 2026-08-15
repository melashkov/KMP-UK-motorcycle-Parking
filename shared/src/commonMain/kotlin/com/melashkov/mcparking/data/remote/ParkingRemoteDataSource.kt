package com.melashkov.mcparking.data.remote

import com.melashkov.mcparking.domain.entity.GeoBounds
import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.ParkingBay
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Singleton

@Singleton
class ParkingRemoteDataSource(
    private val client: HttpClient,
) {
    suspend fun getParkingBays(
        bounds: GeoBounds,
    ): List<ParkingBayDto> {
        return client.get("parking") {
            parameter("north", bounds.north)
            parameter("south", bounds.south)
            parameter("east", bounds.east)
            parameter("west", bounds.west)
        }.body()
    }
}

@Serializable
data class ParkingBayDto(
    val id: String,
    val latitude: Double,
    val longitude: Double,
)

fun ParkingBayDto.toDomain() =
    ParkingBay(
        id = id,
        position = GeoCoordinate(
            latitude = latitude,
            longitude = longitude,
        ),
    )