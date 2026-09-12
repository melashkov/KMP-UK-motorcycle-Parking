package com.melashkov.mcparking.ui.bayEditor

import androidx.compose.runtime.Composable
import com.melashkov.mcparking.domain.interfaces.DataError
import org.jetbrains.compose.resources.stringResource
import ukmotorcycleparking.shared.generated.resources.Res
import ukmotorcycleparking.shared.generated.resources.bay_editor_error_description_too_long
import ukmotorcycleparking.shared.generated.resources.bay_editor_error_location_required
import ukmotorcycleparking.shared.generated.resources.bay_editor_error_name_required
import ukmotorcycleparking.shared.generated.resources.bay_editor_error_name_too_long
import ukmotorcycleparking.shared.generated.resources.bay_editor_error_offline
import ukmotorcycleparking.shared.generated.resources.bay_editor_error_rate_limited
import ukmotorcycleparking.shared.generated.resources.bay_editor_error_server_unavailable
import ukmotorcycleparking.shared.generated.resources.bay_editor_error_type_required
import ukmotorcycleparking.shared.generated.resources.bay_editor_error_unauthorised
import ukmotorcycleparking.shared.generated.resources.bay_editor_error_unknown

@Composable
internal fun DataError.localizedEditorMessage(): String =
    stringResource(
        when (this) {
            DataError.Offline -> Res.string.bay_editor_error_offline
            DataError.ServerUnavailable -> Res.string.bay_editor_error_server_unavailable
            DataError.RateLimited -> Res.string.bay_editor_error_rate_limited
            DataError.Unauthorized,
            DataError.Forbidden -> Res.string.bay_editor_error_unauthorised
            DataError.Unknown -> Res.string.bay_editor_error_unknown
        },
    )

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
