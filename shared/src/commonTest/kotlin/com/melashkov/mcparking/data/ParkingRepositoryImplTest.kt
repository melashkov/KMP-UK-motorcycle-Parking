package com.melashkov.mcparking.data

import com.melashkov.mcparking.data.remote.ParkingRemoteDataSource
import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.ParkingBaySubmission
import com.melashkov.mcparking.domain.entity.ParkingType
import com.melashkov.mcparking.domain.interfaces.AppError
import com.melashkov.mcparking.domain.interfaces.DataResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ParkingRepositoryImplTest {
    @Test
    fun successfulApiStatusReturnsSuccess() = runTest {
        val fixture = repositoryResponding(
            body = """{"status":"OK"}""",
        )

        try {
            assertIs<DataResult.Success<Unit>>(
                fixture.repository.submitParkingBay(testSubmission()),
            )
        } finally {
            fixture.close()
        }
    }

    @Test
    fun rejectedApiStatusReturnsServerFailure() = runTest {
        val fixture = repositoryResponding(
            body = """{"status":"error","message":"Parent parking bay does not exist"}""",
        )

        try {
            assertEquals(
                AppError.ServerError("Parent parking bay does not exist"),
                assertIs<DataResult.Failure>(
                    fixture.repository.submitParkingBay(testSubmission()),
                ).error,
            )
        } finally {
            fixture.close()
        }
    }

    @Test
    fun clientErrorBodyIsPassedThroughAsServerFailure() = runTest {
        val fixture = repositoryResponding(
            body = """{"status":"error","message":"Invalid parking type"}""",
            status = HttpStatusCode.UnprocessableEntity,
        )

        try {
            assertEquals(
                AppError.ServerError("Invalid parking type"),
                assertIs<DataResult.Failure>(
                    fixture.repository.submitParkingBay(testSubmission()),
                ).error,
            )
        } finally {
            fixture.close()
        }
    }

    @Test
    fun serverErrorBodyIsPassedThroughAsServerFailure() = runTest {
        val fixture = repositoryResponding(
            body = """{"status":"error","message":"Please try again later"}""",
            status = HttpStatusCode.InternalServerError,
        )

        try {
            assertEquals(
                AppError.ServerError("Please try again later"),
                assertIs<DataResult.Failure>(
                    fixture.repository.submitParkingBay(testSubmission()),
                ).error,
            )
        } finally {
            fixture.close()
        }
    }

    @Test
    fun serverErrorIsMappedToServerUnavailable() = runTest {
        val fixture = repositoryResponding(
            body = """{"status":"error"}""",
            status = HttpStatusCode.ServiceUnavailable,
        )

        try {
            assertEquals(
                AppError.ServerUnavailable,
                assertIs<DataResult.Failure>(
                    fixture.repository.submitParkingBay(testSubmission()),
                ).error,
            )
        } finally {
            fixture.close()
        }
    }

    @Test
    fun networkIOExceptionIsMappedToConnectionFailed() = runTest {
        val client = HttpClient(
            MockEngine { throw IOException("offline") },
        ) {
            expectSuccess = true
            install(ContentNegotiation) { json() }
            defaultRequest { url("https://parking.test/api/") }
        }
        val repository = ParkingRepositoryImpl(ParkingRemoteDataSource(client))

        try {
            assertEquals(
                AppError.ConnectionFailed,
                assertIs<DataResult.Failure>(
                    repository.submitParkingBay(testSubmission()),
                ).error,
            )
        } finally {
            client.close()
        }
    }

    @Test
    fun malformedResponseIsMappedToUnknownInsteadOfCrashing() = runTest {
        val fixture = repositoryResponding(
            body = """<br /><b>Deprecated</b>: Function curl_close() is deprecated""",
        )

        try {
            assertEquals(
                AppError.Unknown,
                assertIs<DataResult.Failure>(
                    fixture.repository.submitParkingBay(testSubmission()),
                ).error,
            )
        } finally {
            fixture.close()
        }
    }

    private fun repositoryResponding(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ): RepositoryFixture {
        val client = HttpClient(
            MockEngine {
                respond(
                    content = body,
                    status = status,
                    headers = JsonHeaders,
                )
            },
        ) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            defaultRequest { url("https://parking.test/api/") }
        }
        return RepositoryFixture(
            repository = ParkingRepositoryImpl(ParkingRemoteDataSource(client)),
            client = client,
        )
    }

    private fun testSubmission(parentId: String? = null) = ParkingBaySubmission(
        parentId = parentId,
        title = "Waterloo Road",
        description = "Six spaces",
        type = ParkingType.FREE,
        position = GeoCoordinate(51.5074, -0.1278),
    )

    private companion object {
        val JsonHeaders = headersOf(
            HttpHeaders.ContentType,
            ContentType.Application.Json.toString(),
        )
    }

    private data class RepositoryFixture(
        val repository: ParkingRepositoryImpl,
        val client: HttpClient,
    ) {
        fun close() = client.close()
    }
}
