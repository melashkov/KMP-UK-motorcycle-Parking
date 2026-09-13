package com.melashkov.mcparking.di

import org.koin.core.scope.Scope

private const val PRODUCTION_API_URL = "https://melashkov.com/api/"
private const val LOCALHOST_DEVELOPMENT_API_URL = "http://127.0.0.1:8080/api/"
private const val ANDROID_EMULATOR_DEVELOPMENT_API_URL = "http://10.0.2.2:8080/api/"

internal enum class ApiEnvironment {
    Production,
    LocalhostDevelopment,
    AndroidEmulatorDevelopment,
}

interface ApiUrlProvider {
    val baseUrl: String
}

internal fun apiUrlProvider(
    environment: ApiEnvironment,
): ApiUrlProvider =
    DefaultApiUrlProvider(
        baseUrl =
            when (environment) {
                ApiEnvironment.Production -> PRODUCTION_API_URL
                ApiEnvironment.LocalhostDevelopment -> LOCALHOST_DEVELOPMENT_API_URL
                ApiEnvironment.AndroidEmulatorDevelopment -> ANDROID_EMULATOR_DEVELOPMENT_API_URL
            },
    )

private data class DefaultApiUrlProvider(
    override val baseUrl: String,
) : ApiUrlProvider

internal expect fun platformApiUrlProvider(scope: Scope): ApiUrlProvider
