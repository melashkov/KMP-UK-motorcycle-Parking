package com.melashkov.mcparking.di

import androidx.compose.runtime.Composable
import org.koin.core.annotation.PropertyValue

internal const val API_BASE_URL_PROPERTY = "apiBaseUrl"

private const val DEVELOPMENT_API_BASE_URL = "http://127.0.0.1:8080/api_dev/"

@PropertyValue(API_BASE_URL_PROPERTY)
private const val PRODUCTION_API_BASE_URL = "https://melashkov.com/api/"

internal fun apiBaseUrl(isDebug: Boolean): String =
    if (isDebug) DEVELOPMENT_API_BASE_URL else PRODUCTION_API_BASE_URL

@Composable
internal expect fun rememberApiBaseUrl(): String
