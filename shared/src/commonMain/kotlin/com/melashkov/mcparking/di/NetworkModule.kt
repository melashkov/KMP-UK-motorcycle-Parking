package com.melashkov.mcparking.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import org.koin.core.scope.Scope

@Module
class NetworkModule {

    @Singleton
    internal fun provideInstallationIdProvider(scope: Scope): InstallationIdProvider =
        platformInstallationIdProvider(scope)

    @Singleton
    fun provideApiUrlProvider(scope: Scope): ApiUrlProvider =
        platformApiUrlProvider(scope)

    @Singleton
    fun provideApiDebugLogger(scope: Scope): ApiDebugLogger = platformApiDebugLogger(scope)

    @Singleton
    fun provideHttpClient(
        apiUrlProvider: ApiUrlProvider,
        scope: Scope,
        debugLogger: ApiDebugLogger,
    ): HttpClient =
        HttpClient {
            expectSuccess = true

            install(ApiClientMetadataPlugin) {
                metadata = platformApiClientMetadata(scope, scope.get<InstallationIdProvider>().getOrCreate())
            }

            if (debugLogger.enabled) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) = debugLogger.log(message)
                    }
                    level = LogLevel.HEADERS
                }
            }

            install(ContentNegotiation) {
                val json = Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                }
                json(json, ContentType.Application.Json)
                json(json, ContentType.Text.Plain)
                json(json, ContentType.Text.Html)
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
            }

            apiDefaultRequest { apiUrlProvider.baseUrl }
        }
}
