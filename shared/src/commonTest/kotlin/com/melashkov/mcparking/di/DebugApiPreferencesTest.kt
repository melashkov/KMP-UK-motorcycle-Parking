package com.melashkov.mcparking.di

import kotlin.test.Test
import kotlin.test.assertEquals

class DebugApiPreferencesTest {
    @Test
    fun releaseAlwaysUsesProductionEvenWithSavedLocalPreference() {
        assertEquals(ApiEnvironment.Production, environment(enabled = false, local = true))
    }

    @Test
    fun debugDefaultsToProduction() {
        assertEquals(ApiEnvironment.Production, environment(enabled = true, local = false))
    }

    @Test
    fun debugCanSelectPlatformLocalEnvironment() {
        for (local in listOf(ApiEnvironment.AndroidEmulatorDevelopment, ApiEnvironment.LocalhostDevelopment)) {
            assertEquals(local, environment(enabled = true, local = true, localEnvironment = local))
        }
    }

    private fun environment(
        enabled: Boolean,
        local: Boolean,
        localEnvironment: ApiEnvironment = ApiEnvironment.AndroidEmulatorDevelopment,
    ): ApiEnvironment = debugApiEnvironment(
        object : DebugApiPreferences {
            override val enabled = enabled
            override var localDevelopment = local
        },
        localEnvironment,
    )
}
