package com.melashkov.mcparking.di

import io.ktor.client.plugins.api.SendingRequest
import io.ktor.client.plugins.api.createClientPlugin
import kotlinx.coroutines.CancellationException
import org.koin.core.scope.Scope

class ApiDebugLogger(
    val enabled: Boolean = false,
    private val write: (String) -> Unit = {},
) {
    fun log(message: String) {
        if (enabled) write(redact(message))
    }

    suspend fun <T> trace(operation: String, block: suspend () -> T): T = try {
        block()
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        if (enabled) {
            log("$operation failed")
            var cause: Throwable? = error
            repeat(5) {
                val current = cause ?: return@repeat
                // Keep error summaries; JSON parsing errors can embed an entire response body.
                log("${current::class.simpleName}: ${current.message.orEmpty().lineSequence().firstOrNull().orEmpty().take(500)}")
                cause = current.cause.takeUnless { it === current }
            }
        }
        throw error
    }

    private fun redact(message: String): String = message
        .replace(Regex("([?&][A-Za-z0-9_.%-]+=)[^\\s&]+"), "$1<redacted>")
        .replace(Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"), "<installation-id>")
        .replace(Regex("(?i)(Bearer\\s+)[^\\s,;]+"), "$1<redacted>")
        .replace(Regex("(?i)((?:authorization|cookie|set-cookie|x-installation-id)\\s*[:=]\\s*)[^\\r\\n]+"), "$1<redacted>")
}

internal class ApiDebugLoggingConfig {
    var logger: ApiDebugLogger = ApiDebugLogger()
}

internal val ApiDebugLoggingPlugin = createClientPlugin("ApiDebugLogging", ::ApiDebugLoggingConfig) {
    val logger = pluginConfig.logger
    on(SendingRequest) { request, _ ->
        // Omit query values, bodies, credentials and installation IDs.
        val url = request.url.build()
        logger.log("REQUEST ${request.method.value} ${url.protocol.name}://${url.host}:${url.port}${url.encodedPath} query=[${url.parameters.names().joinToString()}]")
        listOf("X-App-Platform", "X-App-Version", "X-App-Version-Code", "X-OS-Version", "X-Android-API-Level").forEach { name ->
            request.headers[name]?.let { logger.log("$name: $it") }
        }
    }
    onResponse { response ->
        logger.log("RESPONSE ${response.status.value} ${response.call.request.url.encodedPath} content-type=${response.headers["Content-Type"] ?: "missing"}")
    }
}

internal expect fun platformApiDebugLogger(scope: Scope): ApiDebugLogger
