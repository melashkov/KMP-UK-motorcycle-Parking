package com.melashkov.mcparking.ui.bayEditor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ukmotorcycleparking.shared.generated.resources.Res
import ukmotorcycleparking.shared.generated.resources.action_done
import ukmotorcycleparking.shared.generated.resources.bay_editor_submit_edit
import ukmotorcycleparking.shared.generated.resources.bay_editor_submit_new
import ukmotorcycleparking.shared.generated.resources.submission_success_add_message
import ukmotorcycleparking.shared.generated.resources.submission_success_edit_message
import ukmotorcycleparking.shared.generated.resources.submission_success_title

@Composable
internal fun EditorSubmitBar(
    mode: BayEditorMode,
    isSubmitting: Boolean,
    submissionError: String?,
    onSubmit: () -> Unit,
) {
    Surface(shadowElevation = 3.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            submissionError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            Button(
                onClick = onSubmit,
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(18.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text(
                    when (mode) {
                        BayEditorMode.Add -> stringResource(Res.string.bay_editor_submit_new)
                        BayEditorMode.Edit -> stringResource(Res.string.bay_editor_submit_edit)
                    },
                )
            }
        }
    }
}

@Composable
internal fun SubmissionSuccessDialog(
    mode: BayEditorMode,
    onDone: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDone,
        title = { Text(stringResource(Res.string.submission_success_title)) },
        text = {
            Text(
                when (mode) {
                    BayEditorMode.Add -> stringResource(
                        Res.string.submission_success_add_message,
                    )
                    BayEditorMode.Edit -> stringResource(
                        Res.string.submission_success_edit_message,
                    )
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onDone) {
                Text(stringResource(Res.string.action_done))
            }
        },
    )
}
