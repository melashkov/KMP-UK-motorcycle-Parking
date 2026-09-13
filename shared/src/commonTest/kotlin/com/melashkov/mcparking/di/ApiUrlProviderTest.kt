package com.melashkov.mcparking.di

import kotlin.test.Test
import kotlin.test.assertEquals

class ApiUrlProviderTest {

    @Test
    fun productionEnvironmentReturnsProductionUrl() {
        assertEquals(
            "https://melashkov.com/api/",
            apiUrlProvider(ApiEnvironment.Production).baseUrl,
        )
    }

    @Test
    fun localhostEnvironmentReturnsLoopbackDevelopmentUrl() {
        assertEquals(
            "http://127.0.0.1:8080/api/",
            apiUrlProvider(ApiEnvironment.LocalhostDevelopment).baseUrl,
        )
    }

    @Test
    fun androidEmulatorEnvironmentReturnsHostAliasDevelopmentUrl() {
        assertEquals(
            "http://10.0.2.2:8080/api/",
            apiUrlProvider(ApiEnvironment.AndroidEmulatorDevelopment).baseUrl,
        )
    }
}
