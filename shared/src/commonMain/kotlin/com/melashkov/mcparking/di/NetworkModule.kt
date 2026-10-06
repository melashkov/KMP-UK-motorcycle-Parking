package com.melashkov.mcparking.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import org.koin.core.scope.Scope

@Module
class NetworkModule {

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
                metadata = platformApiClientMetadata(scope)
                baseUrl = apiUrlProvider.baseUrl
            }

            if (debugLogger.enabled) {
                debugLogger.log("API base URL: ${apiUrlProvider.baseUrl}")
                install(ApiDebugLoggingPlugin) { logger = debugLogger }
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

            defaultRequest {
                url(apiUrlProvider.baseUrl)
            }
        }
}
