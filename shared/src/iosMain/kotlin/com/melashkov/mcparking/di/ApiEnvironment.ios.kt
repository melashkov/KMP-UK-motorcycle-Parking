package com.melashkov.mcparking.di

import androidx.compose.runtime.Composable
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

@OptIn(ExperimentalNativeApi::class)
@Composable
internal actual fun rememberApiBaseUrl(): String =
    apiBaseUrl(Platform.isDebugBinary)
