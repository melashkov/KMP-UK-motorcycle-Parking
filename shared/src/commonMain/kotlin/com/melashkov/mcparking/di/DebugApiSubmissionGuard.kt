package com.melashkov.mcparking.di

import io.ktor.client.plugins.api.SendingRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpMethod
import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

internal class ApiSubmissionBackend(val selection: DebugApiSelection) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<ApiSubmissionBackend>
}

internal class DebugApiSubmissionGuardConfig {
    lateinit var backend: DebugApiBackend
}

internal val DebugApiSubmissionGuard = createClientPlugin(
    "DebugApiSubmissionGuard",
    ::DebugApiSubmissionGuardConfig,
) {
    val backend = pluginConfig.backend
    on(SendingRequest) { request, _ ->
        val selection = currentCoroutineContext()[ApiSubmissionBackend]?.selection
        if (request.method == HttpMethod.Post && selection != null) {
            check(selection == backend.selection.value) { "The API backend changed before submission." }
        }
    }
}
