package com.melashkov.mcparking.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.core.annotation.KoinApplication
import org.koin.dsl.KoinConfiguration
import org.koin.dsl.includes
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinConfiguration as generatedKoinConfiguration

@KoinApplication(
    modules = [AppModule::class, NetworkModule::class]
)
class KoinApp

@Composable
internal fun rememberKoinAppConfiguration(): KoinConfiguration {
    val apiUrlProvider = rememberApiUrlProvider()
    return remember(apiUrlProvider) {
        koinConfiguration {
            includes(generatedKoinConfiguration<KoinApp>())
            modules(
                module {
                    single<ApiUrlProvider> { apiUrlProvider }
                },
            )
        }
    }
}
