package com.melashkov.mcparking.ui.navigation

import androidx.compose.runtime.Composable

internal fun interface ExternalActionHandler {
    fun handle(action: ExternalAction)
}

@Composable
internal expect fun rememberExternalActionHandler(): ExternalActionHandler
