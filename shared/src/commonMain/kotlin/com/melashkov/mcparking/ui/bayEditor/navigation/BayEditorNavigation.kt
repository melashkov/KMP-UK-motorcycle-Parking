package com.melashkov.mcparking.ui.bayEditor.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.melashkov.mcparking.di.DebugApiBackend
import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.ParkingType
import com.melashkov.mcparking.ui.bayEditor.BayEditorInitialData
import com.melashkov.mcparking.ui.bayEditor.BayEditorMode
import com.melashkov.mcparking.ui.bayEditor.BayEditorScreen
import com.melashkov.mcparking.ui.bayEditor.BayEditorViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

internal fun EntryProviderScope<NavKey>.bayEditorEntries() {
    entry<AddBayRoute> { route ->
        BayEditorEntry(
            route = route,
            initialData = BayEditorInitialData(
                mode = BayEditorMode.Add,
                location = GeoCoordinate(route.latitude, route.longitude),
            ),
        )
    }

    entry<EditBayRoute> { route ->
        BayEditorEntry(
            route = route,
            initialData = BayEditorInitialData(
                mode = BayEditorMode.Edit,
                parentId = route.bayId,
                title = route.title,
                description = route.description,
                type = ParkingType.fromInt(route.type).takeIf { it.isReportableType },
                location = GeoCoordinate(route.latitude, route.longitude),
            ),
        )
    }
}

@Composable
private fun BayEditorEntry(route: BayEditorRoute, initialData: BayEditorInitialData) {
    val backend = koinInject<DebugApiBackend>()
    val selection by backend.selection.collectAsStateWithLifecycle()
    if (route.backend != selection || route.backend != backend.selection.value) return

    val viewModel = koinViewModel<BayEditorViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BayEditorScreen(
        initialData = initialData,
        uiState = state,
    )
}

private val ParkingType.isReportableType: Boolean
    get() = when (this) {
        ParkingType.FREE,
        ParkingType.PAY,
        ParkingType.PERMIT,
        ParkingType.UNCATEGORISED,
        ParkingType.INACTIVE -> true

        ParkingType.UNVERIFIED -> false
    }
