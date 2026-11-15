package com.melashkov.mcparking.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

@Composable
internal fun AppCommandHost(
    backStack: NavBackStack<NavKey>,
    navigationSink: AppNavigationSink,
) {
    val externalActionHandler = rememberExternalActionHandler()

    LaunchedEffect(navigationSink, externalActionHandler, backStack) {
        navigationSink.commands.collect { command ->
            when (command) {
                is AppCommand.Navigate -> backStack.add(command.route)
                AppCommand.Back -> backStack.removeLastOrNull()
                is AppCommand.Launch -> externalActionHandler.handle(command.action)
            }
        }
    }
}
