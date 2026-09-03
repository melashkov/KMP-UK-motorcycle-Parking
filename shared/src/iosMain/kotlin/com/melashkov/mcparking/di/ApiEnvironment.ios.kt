package com.melashkov.mcparking.di

import androidx.compose.runtime.Composable
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

private const val DEVELOPMENT_API_BASE_URL = "http://127.0.0.1:8080/api_dev/"

@OptIn(ExperimentalNativeApi::class)
@Composable
internal actual fun rememberApiBaseUrl(): String =
    apiBaseUrl(Platform.isDebugBinary, DEVELOPMENT_API_BASE_URL)
