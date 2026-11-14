package com.melashkov.mcparking.di

import com.melashkov.mcparking.data.preferences.MemoryPreferenceStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.encodedPath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DebugApiPreferencesTest {
    @Test
    fun releaseAlwaysUsesProductionEvenWithSavedLocalPreference() {
        val store = MemoryPreferenceStore()
        store.putBoolean("local_development", true)
        val backend = DebugApiBackend(DebugApiPreferences(store), enabled = false)
        backend.selectLocal(false)

        assertEquals(ApiEnvironment.Production, backend.activeEnvironment(ApiEnvironment.AndroidEmulatorDevelopment))
        assertFalse(backend.selection.value.restartRequired)
        assertTrue(store.getBoolean("local_development"))
    }

    @Test
    fun backendChangesOnlyOnRestartAndChoosingActiveBackendCancelsPendingSwitch() {
        val store = MemoryPreferenceStore()
        val backend = DebugApiBackend(DebugApiPreferences(store), enabled = true)
        val local = ApiEnvironment.LocalhostDevelopment

        backend.selectLocal(true)
        assertTrue(backend.selection.value.restartRequired)
        assertEquals(ApiEnvironment.Production, backend.activeEnvironment(local))

        assertTrue(store.getBoolean("local_development"))
        val restarted = DebugApiBackend(DebugApiPreferences(store), enabled = true)
        assertFalse(restarted.selection.value.restartRequired)
        assertEquals(local, restarted.activeEnvironment(local))

        restarted.selectLocal(false)
        assertTrue(restarted.selection.value.restartRequired)
        assertEquals(local, restarted.activeEnvironment(local))
        restarted.selectLocal(true)
        assertFalse(restarted.selection.value.restartRequired)
    }

    @Test
    fun pendingSwitchBlocksReportsAndFeedbackBeforeTheyReachTheHttpEngine() = runTest {
        val backend = DebugApiBackend(DebugApiPreferences(MemoryPreferenceStore()), enabled = true)
        val requests = mutableListOf<String>()
        val client = HttpClient(MockEngine { request ->
            requests += request.url.encodedPath
            respond("{}")
        }) {
            install(DebugApiSubmissionGuard) { this.backend = backend }
        }

        try {
            val url = "https://example.com/api/"
            backend.selectLocal(true)
            assertFailsWith<IllegalStateException> { client.post(url + "report.php") }
            assertFailsWith<IllegalStateException> { client.post(url + "feedback.php") }
            client.get(url + "bounds.php")
            assertEquals(listOf("/api/bounds.php"), requests)

            backend.selectLocal(false)
            client.post(url + "report.php")
            client.post(url + "feedback.php")
            assertEquals(listOf("/api/bounds.php", "/api/report.php", "/api/feedback.php"), requests)
        } finally {
            client.close()
        }
    }
}
