package com.melashkov.mcparking.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ApiClientMetadataTest {
    private val android = ApiClientMetadata("android", "3.0", "1678468730", "6.0", 23, "993dd131-4ee1-4275-a431-a5fe2a19f450")

    @Test
    fun lookupAndReportRequestsIncludeBuildOsAndInstallationHeaders() = runTest {
        var requests = 0
        val client = client(MockEngine { request ->
            requests++
            android.headers.forEach { (name, value) ->
                assertEquals(listOf(value), request.headers.getAll(name))
            }
            respond("{}")
        })
        try {
            client.get("bounds.php")
            client.post("report.php")
            assertEquals(2, requests)
        } finally { client.close() }
    }

    @Test
    fun externalRedirectDoesNotReceiveInstallationMetadata() = runTest {
        var requests = 0
        val client = client(MockEngine { request ->
            requests++
            if (request.url.host == "melashkov.com") {
                assertEquals(android.installationId, request.headers["X-Installation-ID"])
                respond("", HttpStatusCode.Found, headersOf("Location", "https://example.com/api/bounds.php"))
            } else {
                android.headers.keys.forEach { assertNull(request.headers[it]) }
                respond("{}")
            }
        })
        try {
            client.get("bounds.php")
            assertEquals(2, requests)
        } finally { client.close() }
    }

    @Test
    fun otherPathsAndDowngradedRequestsDoNotReceiveMetadata() = runTest {
        val client = client(MockEngine { request ->
            android.headers.keys.forEach { assertNull(request.headers[it]) }
            respond("{}")
        })
        try {
            client.get("https://melashkov.com/api-other/bounds.php")
            client.get("http://melashkov.com/api/bounds.php")
        } finally { client.close() }
    }

    @Test
    fun iosUsesTheSameBuildHeadersWithoutAndroidApiLevel() = runTest {
        val ios = android.copy(platform = "ios", appVersionCode = "42", osVersion = "18.0", androidApiLevel = null)
        val client = client(MockEngine { request ->
            assertEquals("ios", request.headers["X-App-Platform"])
            assertEquals("42", request.headers["X-App-Version-Code"])
            assertEquals("18.0", request.headers["X-OS-Version"])
            assertNull(request.headers["X-Android-API-Level"])
            respond("{}")
        }, ios)
        try { client.get("bounds.php") } finally { client.close() }
    }

    @Test
    fun redirectToTheNewBackendDoesNotInheritMetadataFromAnEarlierRequest() = runTest {
        var base = "http://10.0.2.2:8080/api/"
        val installationIds = mutableListOf<String?>()
        val client = client(MockEngine { request ->
            installationIds += request.headers["X-Installation-ID"]
            if (request.url.host == "10.0.2.2") {
                base = "https://melashkov.com/api/"
                respond("", HttpStatusCode.Found, headersOf("Location", base + "bounds.php"))
            } else {
                respond("{}")
            }
        }, currentBaseUrl = { base })
        try {
            client.get("bounds.php")
            client.get("bounds.php")
            assertEquals(listOf(android.installationId, null, android.installationId), installationIds)
        } finally {
            client.close()
        }
    }

    private fun client(
        engine: MockEngine,
        values: ApiClientMetadata = android,
        currentBaseUrl: () -> String = { "https://melashkov.com/api/" },
    ) = HttpClient(engine) {
        install(ApiClientMetadataPlugin) { metadata = values }
        apiDefaultRequest(currentBaseUrl)
    }
}
