package com.melashkov.mcparking.di

import io.ktor.client.HttpClient
import org.koin.core.scope.Scope

internal actual fun platformHttpClient(scope: Scope, apiUrlProvider: ApiUrlProvider): HttpClient =
    HttpClient()
