package com.melashkov.mcparking.ui.bayEditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
internal fun BayEditorForm(
    uiState: BayEditorUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        OutlinedTextField(
            value = uiState.title,
            onValueChange = { uiState.eventSink(BayEditorEvent.TitleChanged(it)) },
            label = { Text("Name") },
            placeholder = { Text("e.g. Motorcycle bays outside the station") },
            singleLine = true,
            isError = uiState.titleError != null,
            supportingText = {
                FieldSupportingText(
                    error = uiState.titleError,
                    count = uiState.title.length,
                    maximum = BayEditorViewModel.TITLE_MAX_LENGTH,
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )

        ParkingTypeSelector(
            mode = uiState.mode,
            selected = uiState.type,
            error = uiState.typeError,
            onSelected = { uiState.eventSink(BayEditorEvent.TypeChanged(it)) },
        )

        LocationField(uiState)

        OutlinedTextField(
            value = uiState.description,
            onValueChange = { uiState.eventSink(BayEditorEvent.DescriptionChanged(it)) },
            label = { Text("Description (optional)") },
            placeholder = { Text("Access notes, restrictions, number of spaces…") },
            minLines = 3,
            maxLines = 5,
            isError = uiState.descriptionError != null,
            supportingText = {
                FieldSupportingText(
                    error = uiState.descriptionError,
                    count = uiState.description.length,
                    maximum = BayEditorViewModel.DESCRIPTION_MAX_LENGTH,
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { uiState.eventSink(BayEditorEvent.Submit) },
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        ReviewNotice()
    }
}

@Composable
private fun LocationField(uiState: BayEditorUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Location",
            style = MaterialTheme.typography.titleSmall,
        )
        uiState.location?.let { location ->
            ParkingLocationPreview(
                location = location,
                type = uiState.type,
                error = uiState.locationError,
                onChooseLocation = {
                    uiState.eventSink(BayEditorEvent.ChooseLocation)
                },
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Move the centre pin to the exact parking location",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ReviewNotice() {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = "Your submission will be reviewed before it appears on the map.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun FieldSupportingText(
    error: String?,
    count: Int,
    maximum: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(error.orEmpty())
        Text("$count/$maximum")
    }
}
