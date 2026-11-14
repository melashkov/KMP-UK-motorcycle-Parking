package com.melashkov.mcparking.di

import com.melashkov.mcparking.data.preferences.MemoryPreferenceStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.request.post
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal fun testDebugApiBackend(enabled: Boolean = false): DebugApiBackend =
    DebugApiBackend(DebugApiPreferences(MemoryPreferenceStore()), enabled)

class DebugApiPreferencesTest {
    @Test
    fun releaseAlwaysUsesProductionEvenWithSavedLocalPreference() {
        val store = MemoryPreferenceStore()
        store.putBoolean("local_development", true)
        val backend = DebugApiBackend(DebugApiPreferences(store), enabled = false)
        backend.selectLocal(false)
        backend.selectLocal(true)

        assertEquals(ApiEnvironment.Production, backend.activeEnvironment(ApiEnvironment.AndroidEmulatorDevelopment))
        assertEquals(DebugApiSelection(local = false), backend.selection.value)
        assertTrue(store.getBoolean("local_development"))
    }

    @Test
    fun liveSelectionPersistsAndOnlyChangesRevisionOnAnActualSwitch() {
        val store = MemoryPreferenceStore()
        val backend = DebugApiBackend(DebugApiPreferences(store), enabled = true)
        val local = ApiEnvironment.LocalhostDevelopment

        backend.selectLocal(true)
        backend.selectLocal(true)
        assertEquals(DebugApiSelection(local = true, revision = 1), backend.selection.value)
        assertEquals(local, backend.activeEnvironment(local))
        assertTrue(store.getBoolean("local_development"))
        assertEquals(local, DebugApiBackend(DebugApiPreferences(store), enabled = true).activeEnvironment(local))

        backend.selectLocal(false)
        assertEquals(DebugApiSelection(local = false, revision = 2), backend.selection.value)
        assertEquals(ApiEnvironment.Production, backend.activeEnvironment(local))
    }

    @Test
    fun oneClientRoutesNewRequestsAndRejectsSubmissionsFromAnEarlierSelection() = runTest {
        val backend = testDebugApiBackend(enabled = true)
        val provider = apiUrlProvider { backend.activeEnvironment(ApiEnvironment.AndroidEmulatorDevelopment) }
        val urls = mutableListOf<String>()
        val client = HttpClient(MockEngine { request ->
            urls += request.url.toString()
            respond("{}")
        }) {
            apiDefaultRequest { provider.baseUrl }
            install(DebugApiSubmissionGuard) { this.backend = backend }
        }

        try {
            val original = backend.selection.value
            client.get("bounds.php")
            backend.selectLocal(true)
            assertFailsWith<IllegalStateException> {
                withContext(ApiSubmissionBackend(original)) { client.post("report.php") }
            }
            withContext(ApiSubmissionBackend(backend.selection.value)) { client.post("feedback.php") }
            backend.selectLocal(false)
            assertFailsWith<IllegalStateException> {
                withContext(ApiSubmissionBackend(original)) { client.post("report.php") }
            }
            client.get("bounds.php")
            assertEquals(
                listOf(
                    "https://melashkov.com/api/bounds.php",
                    "http://10.0.2.2:8080/api/feedback.php",
                    "https://melashkov.com/api/bounds.php",
                ),
                urls,
            )
        } finally {
            client.close()
        }
    }
}
