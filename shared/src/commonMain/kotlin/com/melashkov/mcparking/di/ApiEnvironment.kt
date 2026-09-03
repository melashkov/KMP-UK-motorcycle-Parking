package com.melashkov.mcparking.di

import androidx.compose.runtime.Composable
import org.koin.core.annotation.PropertyValue

internal const val API_BASE_URL_PROPERTY = "apiBaseUrl"

@PropertyValue(API_BASE_URL_PROPERTY)
private const val PRODUCTION_API_BASE_URL = "https://melashkov.com/api/"

internal fun apiBaseUrl(
    isDebug: Boolean,
    developmentApiBaseUrl: String,
): String = if (isDebug) developmentApiBaseUrl else PRODUCTION_API_BASE_URL

@Composable
internal expect fun rememberApiBaseUrl(): String
