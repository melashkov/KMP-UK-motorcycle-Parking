package com.melashkov.mcparking.ui.bayEditor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import org.jetbrains.compose.resources.stringResource
import ukmotorcycleparking.shared.generated.resources.Res
import ukmotorcycleparking.shared.generated.resources.action_back
import ukmotorcycleparking.shared.generated.resources.bay_editor_add_title
import ukmotorcycleparking.shared.generated.resources.bay_editor_suggest_edit_title

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Suppress("DEPRECATION")
@Composable
fun BayEditorScreen(uiState: BayEditorUiState) {
    val formScrollState = rememberScrollState()

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
        topBar = {
            EditorTopBar(
                mode = uiState.mode,
                onBack = { uiState.eventSink(BayEditorEvent.Back) },
            )
        },
        bottomBar = {
            if (uiState.isInitialized) {
                EditorSubmitBar(
                    mode = uiState.mode,
                    isSubmitting = uiState.isSubmitting,
                    submissionError = uiState.submissionError,
                    onSubmit = { uiState.eventSink(BayEditorEvent.Submit) },
                )
            }
        },
    ) { contentPadding ->
        if (uiState.isInitialized) {
            BayEditorForm(
                mode = uiState.mode,
                title = uiState.title,
                titleError = uiState.titleError,
                onTitleChanged = {
                    uiState.eventSink(BayEditorEvent.TitleChanged(it))
                },
                type = uiState.type,
                typeError = uiState.typeError,
                onTypeSelected = {
                    uiState.eventSink(BayEditorEvent.TypeChanged(it))
                },
                location = uiState.location,
                locationError = uiState.locationError,
                onChooseLocation = {
                    uiState.eventSink(BayEditorEvent.ChooseLocation)
                },
                description = uiState.description,
                descriptionError = uiState.descriptionError,
                onDescriptionChanged = {
                    uiState.eventSink(BayEditorEvent.DescriptionChanged(it))
                },
                onSubmit = { uiState.eventSink(BayEditorEvent.Submit) },
                scrollState = formScrollState,
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
private fun EditorTopBar(
    mode: BayEditorMode,
    onBack: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                when (mode) {
                    BayEditorMode.Add -> stringResource(Res.string.bay_editor_add_title)
                    BayEditorMode.Edit -> stringResource(
                        Res.string.bay_editor_suggest_edit_title,
                    )
                },
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.action_back),
                )
            }
        },
    )
}
