package com.melashkov.mcparking.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Module
import org.koin.core.annotation.Property
import org.koin.core.annotation.Singleton

@Module
class NetworkModule {

    @Singleton
    fun provideHttpClient(
        @Property(API_BASE_URL_PROPERTY) apiBaseUrl: String,
    ): HttpClient =
        HttpClient {
            expectSuccess = true

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
                url(apiBaseUrl)
            }
        }
}
