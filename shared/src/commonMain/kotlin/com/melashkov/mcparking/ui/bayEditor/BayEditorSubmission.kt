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

@Composable
internal fun EditorSubmitBar(uiState: BayEditorUiState) {
    Surface(shadowElevation = 3.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            uiState.submissionError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            Button(
                onClick = { uiState.eventSink(BayEditorEvent.Submit) },
                enabled = !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(18.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text(
                    when (uiState.mode) {
                        BayEditorMode.Add -> "Submit new bay"
                        BayEditorMode.Edit -> "Submit suggested edit"
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
        title = { Text("Thanks — submitted") },
        text = {
            Text(
                when (mode) {
                    BayEditorMode.Add -> "The new bay has been sent for review."
                    BayEditorMode.Edit -> "Your suggested changes have been sent for review."
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onDone) {
                Text("Done")
            }
        },
    )
}
