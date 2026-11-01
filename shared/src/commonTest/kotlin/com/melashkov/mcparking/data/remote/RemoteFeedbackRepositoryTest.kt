package com.melashkov.mcparking.data.remote

import com.melashkov.mcparking.domain.interfaces.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.*
import io.ktor.http.content.TextContent
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*

class RemoteFeedbackRepositoryTest {
    @Test fun sendsJsonAndRequiresConfirmedSuccess() = runTest {
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("/api/feedback.php", request.url.encodedPath)
            val body = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
            assertEquals("A useful idea", body["message"]?.jsonPrimitive?.content)
            assertEquals("me@example.org", body["email"]?.jsonPrimitive?.content)
            respond("""{"status":"OK"}""", headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        client(engine).use { client ->
            assertIs<DataResult.Success<Unit>>(RemoteFeedbackRepository(client).send("A useful idea", "me@example.org"))
        }
    }

    @Test fun failuresAreNotShownAsSent() = runTest {
        for ((status, body, error) in listOf(
            Triple(HttpStatusCode.TooManyRequests, "{}", AppError.RateLimited),
            Triple(HttpStatusCode.ServiceUnavailable, "{}", AppError.ServerUnavailable),
            Triple(HttpStatusCode.OK, """{"status":"ERROR"}""", AppError.ServerUnavailable),
        )) {
            client(MockEngine { respond(body, status, headersOf(HttpHeaders.ContentType, "application/json")) }).use { client ->
                assertEquals(DataResult.Failure(error), RemoteFeedbackRepository(client).send("A useful idea", ""))
            }
        }
    }

    private fun client(engine: MockEngine) = HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        defaultRequest { url("https://parking.test/api/") }
    }
}
