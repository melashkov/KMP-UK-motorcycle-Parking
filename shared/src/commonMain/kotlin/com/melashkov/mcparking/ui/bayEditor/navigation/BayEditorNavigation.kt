package com.melashkov.mcparking.ui.bayEditor.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.melashkov.mcparking.ui.bayEditor.BayEditorMode
import com.melashkov.mcparking.ui.bayEditor.BayEditorScreen
import com.melashkov.mcparking.ui.navigation.AppRoute
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

@Serializable
data object AddBayRoute : AppRoute

@Serializable
data class EditBayRoute(val bayId: String) : AppRoute

internal fun PolymorphicModuleBuilder<NavKey>.registerBayEditorRoutes() {
    subclass(AddBayRoute::class, AddBayRoute.serializer())
    subclass(EditBayRoute::class, EditBayRoute.serializer())
}

internal fun EntryProviderScope<NavKey>.bayEditorEntries(
    onBack: () -> Unit,
) {
    entry<AddBayRoute> {
        BayEditorScreen(
            mode = BayEditorMode.Add,
            onBack = onBack,
        )
    }

    entry<EditBayRoute> {
        BayEditorScreen(
            mode = BayEditorMode.Edit,
            onBack = onBack,
        )
    }
}
