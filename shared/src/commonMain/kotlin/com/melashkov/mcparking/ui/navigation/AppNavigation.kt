package com.melashkov.mcparking.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
        transitionSpec = {
            (
                slideInHorizontally(
                    animationSpec = tween(ScreenSlideDurationMillis),
                    initialOffsetX = { width -> width },
                ) togetherWith ExitTransition.None
            ).apply {
                targetContentZIndex = 1f
            }
        },
        popTransitionSpec = {
            (
                EnterTransition.None togetherWith slideOutHorizontally(
                    animationSpec = tween(ScreenSlideDurationMillis),
                    targetOffsetX = { width -> width },
                )
            ).apply {
                targetContentZIndex = -1f
            }
        },
        predictivePopTransitionSpec = {
            (
                EnterTransition.None togetherWith slideOutHorizontally(
                    animationSpec = tween(ScreenSlideDurationMillis),
                    targetOffsetX = { width -> width },
                )
            ).apply {
                targetContentZIndex = -1f
            }
        },
        entryProvider = entryProvider {
            baysMapEntries()
            bayEditorEntries()
        },
    )
}

private const val ScreenSlideDurationMillis = 240
