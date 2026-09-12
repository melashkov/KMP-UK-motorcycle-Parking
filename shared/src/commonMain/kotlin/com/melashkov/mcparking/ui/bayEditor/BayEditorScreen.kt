package com.melashkov.mcparking.ui.bayEditor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
fun BayEditorScreen(
    initialData: BayEditorInitialData,
    uiState: BayEditorUiState,
) {
    val formState = rememberBayEditorFormState(initialData)
    val formScrollState = rememberScrollState()
    val submit: () -> Unit = {
        formState.submissionOrNull()?.let { submission ->
            uiState.eventSink(BayEditorEvent.Submit(submission))
        }
    }

    BackHandler(enabled = formState.isLocationPickerOpen) {
        formState.dismissLocationPicker()
    }

    val pickerLocation = formState.location
    if (formState.isLocationPickerOpen && pickerLocation != null) {
        ParkingLocationPicker(
            initialLocation = pickerLocation,
            type = formState.type,
            onBack = formState::dismissLocationPicker,
            onLocationSelected = {
                formState.updateLocation(it)
                uiState.eventSink(BayEditorEvent.ClearSubmissionError)
            },
        )
        return
    }

    Scaffold(
        topBar = {
            EditorTopBar(
                mode = formState.mode,
                onBack = { uiState.eventSink(BayEditorEvent.Back) },
            )
        },
        bottomBar = {
            EditorSubmitBar(
                mode = formState.mode,
                isSubmitting = uiState.isSubmitting,
                submissionError = uiState.submissionError,
                onSubmit = submit,
            )
        },
    ) { contentPadding ->
        BayEditorForm(
            mode = formState.mode,
            title = formState.title,
            titleError = formState.titleError,
            onTitleChanged = {
                formState.updateTitle(it)
                uiState.eventSink(BayEditorEvent.ClearSubmissionError)
            },
            type = formState.type,
            typeError = formState.typeError,
            onTypeSelected = {
                formState.updateType(it)
                uiState.eventSink(BayEditorEvent.ClearSubmissionError)
            },
            location = formState.location,
            locationError = formState.locationError,
            onChooseLocation = formState::showLocationPicker,
            description = formState.description,
            descriptionError = formState.descriptionError,
            onDescriptionChanged = {
                formState.updateDescription(it)
                uiState.eventSink(BayEditorEvent.ClearSubmissionError)
            },
            onSubmit = submit,
            scrollState = formScrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        )
    }

    if (uiState.isSubmitted) {
        SubmissionSuccessDialog(
            mode = formState.mode,
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
