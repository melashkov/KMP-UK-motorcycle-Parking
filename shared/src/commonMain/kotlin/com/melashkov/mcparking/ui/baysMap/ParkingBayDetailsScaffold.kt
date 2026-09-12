package com.melashkov.mcparking.ui.baysMap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.unit.dp
import com.melashkov.mcparking.domain.entity.ParkingBay
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ukmotorcycleparking.shared.generated.resources.Res
import ukmotorcycleparking.shared.generated.resources.accessibility_dismiss_bay_details

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Suppress("DEPRECATION")
@Composable
internal fun ParkingBayDetailsScaffold(
    bay: ParkingBay?,
    onDismissRequest: () -> Unit,
    onNavigate: (ParkingBay) -> Unit,
    onShare: (ParkingBay) -> Unit,
    onSuggestEdit: (ParkingBay) -> Unit,
    onStreetView: (ParkingBay) -> Unit,
    content: @Composable () -> Unit,
) {
    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false,
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(bay?.id, bottomSheetState) {
        if (bay == null) {
            if (bottomSheetState.isVisible) bottomSheetState.hide()
        } else {
            snapshotFlow { bottomSheetState.hasExpandedState }.first { it }
            bottomSheetState.expand()
        }
    }

    LaunchedEffect(bay?.id, bottomSheetState) {
        if (bay == null) return@LaunchedEffect

        snapshotFlow { bottomSheetState.currentValue }
            .dropWhile { it == SheetValue.Hidden }
            .first { it == SheetValue.Hidden }
        onDismissRequest()
    }

    fun hideSheet() {
        coroutineScope.launch { bottomSheetState.hide() }
    }

    BackHandler(enabled = bay != null && bottomSheetState.isVisible) {
        hideSheet()
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetSwipeEnabled = bay != null,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetContent = {
            bay?.let { selectedBay ->
                ParkingBayDetailsSheetContent(
                    bay = selectedBay,
                    onNavigate = { onNavigate(selectedBay) },
                    onShare = { onShare(selectedBay) },
                    onSuggestEdit = { onSuggestEdit(selectedBay) },
                    onStreetView = { onStreetView(selectedBay) },
                )
            }
        },
    ) {
        Box(Modifier.fillMaxSize()) {
            content()

            if (bay != null && bottomSheetState.isVisible) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                        .clickable(
                            onClickLabel = stringResource(
                                Res.string.accessibility_dismiss_bay_details,
                            ),
                            onClick = ::hideSheet,
                        ),
                )
            }
        }
    }
}
