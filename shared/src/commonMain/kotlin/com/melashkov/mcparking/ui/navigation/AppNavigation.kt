package com.melashkov.mcparking.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.melashkov.mcparking.ui.bayEditor.navigation.bayEditorEntries
import com.melashkov.mcparking.ui.baysMap.navigation.BaysMapRoute
import com.melashkov.mcparking.ui.baysMap.navigation.baysMapEntries
import org.koin.compose.koinInject

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(
        navigationSavedStateConfiguration,
        BaysMapRoute,
    )
    val navigationSink = koinInject<AppNavigationSink>()

    AppCommandHost(
        backStack = backStack,
        navigationSink = navigationSink,
    )

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            baysMapEntries()
            bayEditorEntries(onBack = { backStack.removeLastOrNull() })
        },
    )
}
