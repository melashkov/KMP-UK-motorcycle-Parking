package com.melashkov.mcparking.data.remote

import com.melashkov.mcparking.data.remote.dto.ParkingResponseDto
import com.melashkov.mcparking.data.remote.dto.ParkingReportRequestDto
import com.melashkov.mcparking.data.remote.dto.ParkingReportResponseDto
import com.melashkov.mcparking.domain.entity.GeoBounds
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.koin.core.annotation.Singleton

@Singleton
class ParkingRemoteDataSource(
    private val client: HttpClient,
) {
    suspend fun getParkingBays(
        bounds: GeoBounds,
    ): ParkingResponseDto {
        return client.get("bounds.php") {
            parameter("north", bounds.north)
            parameter("south", bounds.south)
            parameter("east", bounds.east)
            parameter("west", bounds.west)
        }.body()
    }

    suspend fun submitParkingBay(
        report: ParkingReportRequestDto,
    ): ParkingReportResponseDto =
        client.post("report.php") {
            contentType(ContentType.Application.Json)
            setBody(report)
        }.body()
}
