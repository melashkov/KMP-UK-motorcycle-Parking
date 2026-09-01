package com.melashkov.mcparking.ui.baysMap.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.melashkov.mcparking.ui.baysMap.BaysMap
import com.melashkov.mcparking.ui.baysMap.BaysMapViewModel
import com.melashkov.mcparking.ui.navigation.AppRoute
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object BaysMapRoute : AppRoute

internal fun PolymorphicModuleBuilder<NavKey>.registerBaysMapRoutes() {
    subclass(BaysMapRoute::class, BaysMapRoute.serializer())
}

internal fun EntryProviderScope<NavKey>.baysMapEntries() {
    entry<BaysMapRoute> {
        val viewModel = koinViewModel<BaysMapViewModel>()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        BaysMap(
            uiState = state
        )
    }
}
