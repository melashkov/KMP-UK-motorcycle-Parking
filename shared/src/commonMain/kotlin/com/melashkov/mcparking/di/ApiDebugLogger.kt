package com.melashkov.mcparking.di

import kotlinx.coroutines.CancellationException
import org.koin.core.scope.Scope

class ApiDebugLogger(
    val enabled: Boolean = false,
    private val write: (String) -> Unit = {},
) {
    fun log(message: String) {
        if (enabled) write(message)
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

}

internal expect fun platformApiDebugLogger(scope: Scope): ApiDebugLogger
