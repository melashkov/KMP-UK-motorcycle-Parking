package com.melashkov.mcparking.di

import com.melashkov.mcparking.data.preferences.MemoryPreferenceStoreStub
import com.melashkov.mcparking.data.preferences.PreferenceStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.request.post
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DebugApiPreferencesTest {
    @Test
    fun releaseAlwaysUsesProductionEvenWithSavedLocalPreference() {
        val store = MemoryPreferenceStoreStub()
        store.putBoolean("local_development", true)
        val preferences = DebugApiPreferences(store, isDebug = false)

        assertFalse(preferences.selectLocal(true))
        assertFalse(preferences.selectLocal(false))
        assertFalse(preferences.local)
        assertTrue(store.getBoolean("local_development"))
    }

    @Test
    fun successfulSelectionUpdatesTheCacheAndPersists() {
        val store = MemoryPreferenceStoreStub()
        val preferences = DebugApiPreferences(store, isDebug = true)
        assertTrue(preferences.selectLocal(true))
        assertTrue(preferences.local)
        assertTrue(preferences.selectLocal(true))
        assertTrue(DebugApiPreferences(store, isDebug = true).local)
        assertTrue(preferences.selectLocal(false))
        assertFalse(preferences.local)
        assertFalse(store.getBoolean("local_development"))
    }

    @Test
    fun failedWriteKeepsThePreviousSelection() {
        val savedPreferences = MemoryPreferenceStoreStub().apply { putBoolean("local_development", true) }
        val store = object : PreferenceStore by savedPreferences {
            override fun putBoolean(key: String, value: Boolean) {
                error("Write failed")
            }
        }
        val preferences = DebugApiPreferences(store, isDebug = true)

        assertFalse(preferences.selectLocal(false))
        assertTrue(preferences.local)
    }

    @Test
    fun oneClientRoutesNewRequestsToTheSelectedBackend() = runTest {
        val preferences = DebugApiPreferences(MemoryPreferenceStoreStub(), isDebug = true)
        val provider = apiUrlProvider(ApiEnvironment.AndroidEmulatorDevelopment) { preferences.local }
        val urls = mutableListOf<String>()
        val client = HttpClient(MockEngine { request ->
            urls += request.url.toString()
            respond("{}")
        }) {
            apiDefaultRequest { provider.baseUrl }
        }

        try {
            client.get("bounds.php")
            preferences.selectLocal(true)
            client.post("report.php")
            client.post("feedback.php")
            preferences.selectLocal(false)
            client.get("bounds.php")
            assertEquals(
                listOf(
                    "https://melashkov.com/api/bounds.php",
                    "http://10.0.2.2:8080/api/report.php",
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
