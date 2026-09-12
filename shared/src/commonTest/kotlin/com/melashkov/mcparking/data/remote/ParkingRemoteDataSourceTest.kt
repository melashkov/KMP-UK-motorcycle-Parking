package com.melashkov.mcparking.data.remote

import com.melashkov.mcparking.TestBounds
import com.melashkov.mcparking.data.remote.dto.ParkingReportRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ParkingRemoteDataSourceTest {
    @Test
    fun boundsRequestSendsEveryCoordinateAndDecodesResponse() = runTest {
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("/api/bounds.php", request.url.encodedPath)
            assertEquals(TestBounds.north.toString(), request.url.parameters["north"])
            assertEquals(TestBounds.south.toString(), request.url.parameters["south"])
            assertEquals(TestBounds.east.toString(), request.url.parameters["east"])
            assertEquals(TestBounds.west.toString(), request.url.parameters["west"])
            respond(
                content = BoundsResponse,
                headers = JsonHeaders,
            )
        }
        val client = testClient(engine)

        try {
            val response = ParkingRemoteDataSource(client).getParkingBays(TestBounds)

            assertEquals("OK", response.status)
            assertEquals(1, response.results.size)
            assertEquals("Waterloo Road", response.results.single().title)
        } finally {
            client.close()
        }
    }

    @Test
    fun reportRequestMatchesExistingPhpContract() = runTest {
        val json = Json { ignoreUnknownKeys = true }
        val expected = ParkingReportRequestDto(
            title = "Waterloo Road",
            description = "Six spaces",
            type = 2,
            latitude = 51.5074,
            longitude = -0.1278,
            parentId = "42",
        )
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("/api/report.php", request.url.encodedPath)
            assertEquals(ContentType.Application.Json, request.body.contentType)
            val body = assertIs<TextContent>(request.body).text
            assertEquals(expected, json.decodeFromString<ParkingReportRequestDto>(body))
            respond(
                content = """{"status":"OK"}""",
                headers = JsonHeaders,
            )
        }
        val client = testClient(engine)

        try {
            val response = ParkingRemoteDataSource(client).submitParkingBay(expected)

            assertEquals("OK", response.status)
        } finally {
            client.close()
        }
    }

    private fun testClient(engine: MockEngine) = HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        defaultRequest {
            url("https://parking.test/api/")
        }
    }

    private companion object {
        val JsonHeaders = headersOf(
            HttpHeaders.ContentType,
            ContentType.Application.Json.toString(),
        )

        const val BoundsResponse = """
            {
              "status": "OK",
              "count": 1,
              "limit": 200,
              "bounds": {
                "north": 51.52,
                "south": 51.49,
                "east": -0.10,
                "west": -0.15
              },
              "results": [
                {
                  "id": 42,
                  "title": "Waterloo Road",
                  "type": 1,
                  "latitude": 51.5074,
                  "longitude": -0.1278,
                  "description": "Six spaces"
                }
              ]
            }
        """
    }
}
