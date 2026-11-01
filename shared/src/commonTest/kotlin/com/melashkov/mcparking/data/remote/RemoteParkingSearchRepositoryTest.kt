package com.melashkov.mcparking.data.remote

import com.melashkov.mcparking.TestBounds
import com.melashkov.mcparking.domain.entity.ParkingBay
import com.melashkov.mcparking.domain.interfaces.AppError
import com.melashkov.mcparking.domain.interfaces.DataResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

class RemoteParkingSearchRepositoryTest {
    @Test
    fun boundsRequestSendsEveryCoordinateAndDecodesResponse() = runTest {
        val engine = MockEngine { request ->
            assertNull(request.headers[HttpHeaders.Authorization])
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
            val response = RemoteParkingSearchRepository(client).getParkingBays(TestBounds)

            val bays = assertIs<DataResult.Success<List<ParkingBay>>>(response).value
            assertEquals(1, bays.size)
            assertEquals("Waterloo Road", bays.single().title)
        } finally {
            client.close()
        }
    }

    @Test
    fun rateLimitResponseDoesNotTriggerRegistrationOrImmediateRetries() = runTest {
        var requests = 0
        val client = testClient(MockEngine { request ->
            requests++
            assertEquals("/api/bounds.php", request.url.encodedPath)
            assertNull(request.headers[HttpHeaders.Authorization])
            respond(
                content = """{"status":"ERROR","message":"Usage limit reached"}""",
                status = HttpStatusCode.TooManyRequests,
                headers = JsonHeaders,
            )
        })
        try {
            assertEquals(
                DataResult.Failure(AppError.RateLimited),
                RemoteParkingSearchRepository(client).getParkingBays(TestBounds),
            )
            assertEquals(1, requests)
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
