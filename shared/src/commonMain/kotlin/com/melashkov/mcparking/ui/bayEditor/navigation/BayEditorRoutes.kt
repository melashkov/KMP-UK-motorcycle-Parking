package com.melashkov.mcparking.ui.bayEditor.navigation

import androidx.navigation3.runtime.NavKey
import com.melashkov.mcparking.di.DebugApiSelection
import com.melashkov.mcparking.ui.navigation.AppRoute
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

internal interface BayEditorRoute : AppRoute {
    val backend: DebugApiSelection
}

@Serializable
internal data class AddBayRoute(
    val latitude: Double,
    val longitude: Double,
    override val backend: DebugApiSelection = DebugApiSelection(local = false),
) : BayEditorRoute

@Serializable
internal data class EditBayRoute(
    val bayId: String,
    val title: String,
    val description: String,
    val type: Int,
    val latitude: Double,
    val longitude: Double,
    override val backend: DebugApiSelection = DebugApiSelection(local = false),
) : BayEditorRoute

internal fun PolymorphicModuleBuilder<NavKey>.registerBayEditorRoutes() {
    subclass(AddBayRoute::class, AddBayRoute.serializer())
    subclass(EditBayRoute::class, EditBayRoute.serializer())
}
