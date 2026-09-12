package com.melashkov.mcparking.ui.bayEditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.ScrollState
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
import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.ParkingType
import org.jetbrains.compose.resources.stringResource
import ukmotorcycleparking.shared.generated.resources.Res
import ukmotorcycleparking.shared.generated.resources.bay_editor_character_count
import ukmotorcycleparking.shared.generated.resources.bay_editor_description_label
import ukmotorcycleparking.shared.generated.resources.bay_editor_description_placeholder
import ukmotorcycleparking.shared.generated.resources.bay_editor_location_help
import ukmotorcycleparking.shared.generated.resources.bay_editor_location_label
import ukmotorcycleparking.shared.generated.resources.bay_editor_name_label
import ukmotorcycleparking.shared.generated.resources.bay_editor_name_placeholder
import ukmotorcycleparking.shared.generated.resources.bay_editor_review_notice

@Composable
internal fun BayEditorForm(
    mode: BayEditorMode,
    title: String,
    titleError: BayEditorFieldError?,
    onTitleChanged: (String) -> Unit,
    type: ParkingType?,
    typeError: BayEditorFieldError?,
    onTypeSelected: (ParkingType) -> Unit,
    location: GeoCoordinate?,
    locationError: BayEditorFieldError?,
    onChooseLocation: () -> Unit,
    description: String,
    descriptionError: BayEditorFieldError?,
    onDescriptionChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        NameField(
            value = title,
            error = titleError,
            onValueChanged = onTitleChanged,
        )

        ParkingTypeSelector(
            mode = mode,
            selected = type,
            error = typeError,
            onSelected = onTypeSelected,
        )

        LocationField(
            location = location,
            type = type,
            error = locationError,
            onChooseLocation = onChooseLocation,
        )

        DescriptionField(
            value = description,
            error = descriptionError,
            onValueChanged = onDescriptionChanged,
            onSubmit = onSubmit,
        )

        ReviewNotice()
    }
}

@Composable
private fun NameField(
    value: String,
    error: BayEditorFieldError?,
    onValueChanged: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        label = { Text(stringResource(Res.string.bay_editor_name_label)) },
        placeholder = {
            Text(stringResource(Res.string.bay_editor_name_placeholder))
        },
        singleLine = true,
        isError = error != null,
        supportingText = {
            FieldSupportingText(
                error = error,
                count = value.length,
                maximum = BayEditorFormState.TITLE_MAX_LENGTH,
            )
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun LocationField(
    location: GeoCoordinate?,
    type: ParkingType?,
    error: BayEditorFieldError?,
    onChooseLocation: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(Res.string.bay_editor_location_label),
            style = MaterialTheme.typography.titleSmall,
        )
        location?.let {
            ParkingLocationPreview(
                location = it,
                type = type,
                error = error?.localizedEditorMessage(),
                onChooseLocation = onChooseLocation,
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
                text = stringResource(Res.string.bay_editor_location_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DescriptionField(
    value: String,
    error: BayEditorFieldError?,
    onValueChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        label = { Text(stringResource(Res.string.bay_editor_description_label)) },
        placeholder = {
            Text(stringResource(Res.string.bay_editor_description_placeholder))
        },
        minLines = 3,
        maxLines = 5,
        isError = error != null,
        supportingText = {
            FieldSupportingText(
                error = error,
                count = value.length,
                maximum = BayEditorFormState.DESCRIPTION_MAX_LENGTH,
            )
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        modifier = Modifier.fillMaxWidth(),
    )
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
                text = stringResource(Res.string.bay_editor_review_notice),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun FieldSupportingText(
    error: BayEditorFieldError?,
    count: Int,
    maximum: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(error?.localizedEditorMessage().orEmpty())
        Text(stringResource(Res.string.bay_editor_character_count, count, maximum))
    }
}
