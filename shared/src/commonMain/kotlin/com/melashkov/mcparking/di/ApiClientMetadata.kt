package com.melashkov.mcparking.di

import io.ktor.client.plugins.api.SendingRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.Url
import io.ktor.http.encodedPath
import org.koin.core.scope.Scope

internal data class ApiClientMetadata(
    val platform: String,
    val appVersion: String,
    val appVersionCode: String,
    val osVersion: String,
    val androidApiLevel: Int? = null,
    val installationId: String,
) {
    val headers: Map<String, String> get() = buildMap {
        put("X-App-Platform", platform)
        put("X-App-Version", appVersion)
        put("X-App-Version-Code", appVersionCode)
        put("X-OS-Version", osVersion)
        androidApiLevel?.let { put("X-Android-API-Level", it.toString()) }
        put("X-Installation-ID", installationId)
    }
}

internal class ApiClientMetadataConfig {
    lateinit var metadata: ApiClientMetadata
    lateinit var baseUrl: String
}

internal val ApiClientMetadataPlugin = createClientPlugin("ApiClientMetadata", ::ApiClientMetadataConfig) {
    val api = Url(pluginConfig.baseUrl)
    val values = pluginConfig.metadata.headers
    on(SendingRequest) { request, _ ->
        // Recheck redirects too: installation metadata must stay on our API.
        values.keys.forEach { request.headers.remove(it) }
        request.headers.remove("X-Android-API-Level")
        if (request.url.protocol == api.protocol && request.url.host == api.host &&
            request.url.port == api.port && request.url.encodedPath.startsWith(api.encodedPath)
        ) {
            values.forEach { (name, value) -> request.headers.append(name, value) }
        }
    }
}

internal expect fun platformApiClientMetadata(scope: Scope): ApiClientMetadata
