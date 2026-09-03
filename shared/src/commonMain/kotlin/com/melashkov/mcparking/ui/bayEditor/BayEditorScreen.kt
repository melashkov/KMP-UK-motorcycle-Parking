package com.melashkov.mcparking.ui.bayEditor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Suppress("DEPRECATION")
@Composable
fun BayEditorScreen(uiState: BayEditorUiState) {
    BackHandler(enabled = uiState.isLocationPickerOpen) {
        uiState.eventSink(BayEditorEvent.DismissLocationPicker)
    }

    if (uiState.isLocationPickerOpen && uiState.location != null) {
        ParkingLocationPicker(
            initialLocation = uiState.location,
            type = uiState.type,
            onBack = { uiState.eventSink(BayEditorEvent.DismissLocationPicker) },
            onLocationSelected = {
                uiState.eventSink(BayEditorEvent.LocationSelected(it))
            },
        )
        return
    }

    Scaffold(
        topBar = { EditorTopBar(uiState) },
        bottomBar = {
            if (uiState.isInitialized) {
                EditorSubmitBar(uiState)
            }
        },
    ) { contentPadding ->
        if (uiState.isInitialized) {
            BayEditorForm(
                uiState = uiState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }

    if (uiState.isSubmitted) {
        SubmissionSuccessDialog(
            mode = uiState.mode,
            onDone = { uiState.eventSink(BayEditorEvent.Done) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorTopBar(uiState: BayEditorUiState) {
    TopAppBar(
        title = {
            Text(
                when (uiState.mode) {
                    BayEditorMode.Add -> "Add parking bay"
                    BayEditorMode.Edit -> "Suggest an edit"
                },
            )
        },
        navigationIcon = {
            IconButton(onClick = { uiState.eventSink(BayEditorEvent.Back) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        },
    )
}
