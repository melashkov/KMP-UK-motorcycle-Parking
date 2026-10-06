package com.melashkov.mcparking.di

import com.melashkov.mcparking.TestBounds
import com.melashkov.mcparking.data.remote.ParkingRemoteDataSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApiDebugLoggerTest {
    @Test
    fun disabledLoggerProducesNothingEvenOnFailure() = runTest {
        val messages = mutableListOf<String>()
        val logger = ApiDebugLogger(false, messages::add)
        logger.log("A request")
        assertFailsWith<IllegalStateException> {
            logger.trace("Parking lookup") { error("Failed") }
        }
        assertTrue(messages.isEmpty())
    }

    @Test
    fun failedConnectionsKeepTheirCauseAndIdentifierButRedactTokens() = runTest {
        val messages = mutableListOf<String>()
        val logger = ApiDebugLogger(true, messages::add)
        assertFailsWith<IllegalStateException> {
            logger.trace("Parking lookup") {
                throw IllegalStateException("Bearer secret-token", IllegalArgumentException("993dd131-4ee1-4275-a431-a5fe2a19f450"))
            }
        }
        val text = messages.joinToString()
        assertTrue(text.contains("Parking lookup failed"))
        assertTrue(text.contains("IllegalArgumentException"))
        assertTrue(!text.contains("secret-token") && text.contains("993dd131"))
        assertEquals(1, messages.size)
        logger.log("Request failed: https://melashkov.com/api/bounds.php?north=51.512&south=51.50")
        assertTrue(!messages.last().contains("51.512") && !messages.last().contains("51.50"))
    }

    @Test
    fun malformedServerDataLogsStatusContentTypeAndDecodeFailure() = runTest {
        val messages = mutableListOf<String>()
        val logger = ApiDebugLogger(true, messages::add)
        val client = HttpClient(MockEngine {
            respond("not JSON", headers = headersOf("Content-Type" to listOf("application/json"), "X-Client-Metadata-Status" to listOf("stored")))
        }) {
            install(Logging) {
                this.logger = object : Logger {
                    override fun log(message: String) = logger.log(message)
                }
                level = LogLevel.HEADERS
                sanitizeHeader { it.equals("Authorization", ignoreCase = true) }
            }
            install(ContentNegotiation) { json() }
            defaultRequest { url("https://melashkov.com/api/") }
        }
        try {
            assertFailsWith<Exception> { ParkingRemoteDataSource(client, logger).getParkingBays(TestBounds) }
            val text = messages.joinToString()
            assertTrue(text.contains("REQUEST: https://melashkov.com/api/bounds.php"))
            assertTrue(text.contains("RESPONSE: 200"))
            assertTrue(text.contains("Content-Type: application/json", ignoreCase = true))
            assertTrue(text.contains("Parking lookup failed"))
            assertTrue(text.contains("X-Client-Metadata-Status: stored", ignoreCase = true))
            assertTrue(!text.contains("north=${TestBounds.north}"))
        } finally { client.close() }
    }

    @Test
    fun stockLoggingIncludesMetadataAndOmitsBodiesAndCredentials() = runTest {
        val messages = mutableListOf<String>()
        val logger = ApiDebugLogger(true, messages::add)
        val id = "993dd131-4ee1-4275-a431-a5fe2a19f450"
        val client = HttpClient(MockEngine { respond("response-body-sentinel") }) {
            install(ApiClientMetadataPlugin) {
                metadata = ApiClientMetadata("android", "3.0-dev", "1678468730", "17", 37, id)
                baseUrl = "https://melashkov.com/api/"
            }
            install(Logging) {
                this.logger = object : Logger {
                    override fun log(message: String) = logger.log(message)
                }
                level = LogLevel.HEADERS
                sanitizeHeader { it.equals("Authorization", ignoreCase = true) }
            }
            defaultRequest { url("https://melashkov.com/api/") }
        }
        try {
            client.get("bounds.php") { headers.append("Authorization", "Bearer secret-token") }
            val request = messages.single { it.startsWith("REQUEST") }
            assertTrue(request.contains("X-Installation-ID: $id", ignoreCase = true))
            assertTrue(request.contains("X-App-Version: 3.0-dev", ignoreCase = true))
            assertTrue(request.contains("X-Android-API-Level: 37", ignoreCase = true))
            assertTrue(request.contains('\n'))
            assertTrue(messages.none { it.contains("secret-token") || it.contains("response-body-sentinel") })
            assertEquals(2, messages.size)
        } finally { client.close() }
    }
}
