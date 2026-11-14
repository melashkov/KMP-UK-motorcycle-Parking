package com.melashkov.mcparking.di

import io.ktor.client.plugins.api.SendingRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpMethod

internal class DebugApiSubmissionGuardConfig {
    lateinit var backend: DebugApiBackend
}

internal val DebugApiSubmissionGuard = createClientPlugin(
    "DebugApiSubmissionGuard",
    ::DebugApiSubmissionGuardConfig,
) {
    val backend = pluginConfig.backend
    on(SendingRequest) { request, _ ->
        if (request.method == HttpMethod.Post) {
            check(!backend.selection.value.restartRequired) { "Restart the app to switch API backends." }
        }
    }
}
