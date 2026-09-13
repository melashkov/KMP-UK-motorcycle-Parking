package com.melashkov.mcparking.ui.bayEditor

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ukmotorcycleparking.shared.generated.resources.Res
import ukmotorcycleparking.shared.generated.resources.bay_editor_error_description_too_long
import ukmotorcycleparking.shared.generated.resources.bay_editor_error_location_required
import ukmotorcycleparking.shared.generated.resources.bay_editor_error_name_required
import ukmotorcycleparking.shared.generated.resources.bay_editor_error_name_too_long
import ukmotorcycleparking.shared.generated.resources.bay_editor_error_type_required

@Composable
internal fun BayEditorFieldError.localizedEditorMessage(): String =
    when (this) {
        BayEditorFieldError.TitleRequired ->
            stringResource(Res.string.bay_editor_error_name_required)
        BayEditorFieldError.TitleTooLong ->
            stringResource(
                Res.string.bay_editor_error_name_too_long,
                BayEditorFormState.TITLE_MAX_LENGTH,
            )
        BayEditorFieldError.DescriptionTooLong ->
            stringResource(
                Res.string.bay_editor_error_description_too_long,
                BayEditorFormState.DESCRIPTION_MAX_LENGTH,
            )
        BayEditorFieldError.TypeRequired ->
            stringResource(Res.string.bay_editor_error_type_required)
        BayEditorFieldError.LocationRequired ->
            stringResource(Res.string.bay_editor_error_location_required)
    }
