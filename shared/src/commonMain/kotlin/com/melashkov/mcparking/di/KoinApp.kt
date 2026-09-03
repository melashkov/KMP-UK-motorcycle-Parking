package com.melashkov.mcparking.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.core.annotation.KoinApplication
import org.koin.dsl.KoinConfiguration
import org.koin.dsl.includes
import org.koin.dsl.koinConfiguration
import org.koin.plugin.module.dsl.koinConfiguration as generatedKoinConfiguration

@KoinApplication(
    modules = [AppModule::class, NetworkModule::class]
)
class KoinApp

@Composable
internal fun rememberKoinAppConfiguration(): KoinConfiguration {
    val apiBaseUrl = rememberApiBaseUrl()
    return remember(apiBaseUrl) {
        koinConfiguration {
            includes(generatedKoinConfiguration<KoinApp>())
            properties(mapOf(API_BASE_URL_PROPERTY to apiBaseUrl))
        }
    }
}
