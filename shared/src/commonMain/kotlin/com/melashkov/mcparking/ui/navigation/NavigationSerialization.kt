package com.melashkov.mcparking.ui.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.melashkov.mcparking.ui.bayEditor.navigation.registerBayEditorRoutes
import com.melashkov.mcparking.ui.baysMap.navigation.registerBaysMapRoutes
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

internal val navigationSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            registerBaysMapRoutes()
            registerBayEditorRoutes()
        }
    }
}
