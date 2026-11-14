package com.melashkov.mcparking.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.melashkov.mcparking.di.DebugApiBackend
import com.melashkov.mcparking.ui.bayEditor.navigation.BayEditorRoute
import org.koin.compose.koinInject

@Composable
internal fun AppCommandHost(
    backStack: NavBackStack<NavKey>,
    navigationSink: AppNavigationSink,
) {
    val externalActionHandler = rememberExternalActionHandler()
    val backend = koinInject<DebugApiBackend>()

    LaunchedEffect(backStack, backend) {
        backend.selection.collect { selection ->
            backStack.removeAll { it is BayEditorRoute && it.backend != selection }
        }
    }

    LaunchedEffect(navigationSink, externalActionHandler, backStack, backend) {
        navigationSink.commands.collect { command ->
            when (command) {
                is AppCommand.Navigate -> {
                    val route = command.route
                    if (route !is BayEditorRoute || route.backend == backend.selection.value) {
                        backStack.add(route)
                    }
                }
                AppCommand.Back -> if (backStack.size > 1) backStack.removeLastOrNull()
                is AppCommand.Launch -> externalActionHandler.handle(command.action)
            }
        }
    }
}
