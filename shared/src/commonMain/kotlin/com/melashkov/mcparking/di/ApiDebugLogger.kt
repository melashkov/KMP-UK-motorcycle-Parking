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
            var cause: Throwable? = error
            val causes = mutableListOf<String>()
            repeat(5) {
                val current = cause ?: return@repeat
                // Keep error summaries; JSON parsing errors can embed an entire response body.
                causes += "${current::class.simpleName}: ${current.message.orEmpty().lineSequence().firstOrNull().orEmpty().take(500)}"
                cause = current.cause.takeUnless { it === current }
            }
            log("$operation failed: ${causes.joinToString(" <- ")}")
        }
        throw error
    }

    private fun redact(message: String): String = message
        .replace(Regex("([?&][A-Za-z0-9_.%-]+=)[^\\s&]+"), "$1<redacted>")
        .replace(Regex("(?i)(Bearer\\s+)[^\\s,;]+"), "$1<redacted>")
        .replace(Regex("(?i)((?:authorization|cookie|set-cookie)\\s*[:=]\\s*)[^\\r\\n]+"), "$1<redacted>")
}

internal class ApiDebugLoggingConfig {
    var logger: ApiDebugLogger = ApiDebugLogger()
}

internal val ApiDebugLoggingPlugin = createClientPlugin("ApiDebugLogging", ::ApiDebugLoggingConfig) {
    val logger = pluginConfig.logger
    on(SendingRequest) { request, _ ->
        // Debug-only metadata, including the installation ID; omit bodies and credentials.
        val url = request.url.build()
        val metadata = listOf("X-App-Platform", "X-App-Version", "X-App-Version-Code", "X-OS-Version", "X-Android-API-Level", "X-Installation-ID").mapNotNull { name ->
            request.headers[name]?.let { "$name=$it" }
        }
        logger.log("REQUEST ${request.method.value} ${url.protocol.name}://${url.host}:${url.port}${url.encodedPath} query=[${url.parameters.names().joinToString()}] ${metadata.joinToString(" ")}")
    }
    onResponse { response ->
        logger.log("RESPONSE ${response.status.value} ${response.call.request.url.encodedPath} content-type=${response.headers["Content-Type"] ?: "missing"} metadata-storage=${response.headers["X-Client-Metadata-Status"] ?: "not reported by server"}")
    }
}

internal expect fun platformApiDebugLogger(scope: Scope): ApiDebugLogger
