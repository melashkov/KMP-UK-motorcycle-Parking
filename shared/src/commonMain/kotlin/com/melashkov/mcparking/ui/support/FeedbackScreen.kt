package com.melashkov.mcparking.ui.support

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.melashkov.mcparking.domain.interfaces.AppError
import com.melashkov.mcparking.ui.shared.localizedMessage
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ukmotorcycleparking.shared.generated.resources.*

@Composable
internal fun FeedbackScreen(onDismiss: () -> Unit, viewModel: FeedbackViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    FeedbackContent(state = state, onDismiss = onDismiss)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeedbackContent(state: FeedbackUiState, onDismiss: () -> Unit) {
    var confirmDiscard by rememberSaveable { mutableStateOf(false) }
    val close = { state.eventSink(FeedbackEvent.Reset); onDismiss() }
    val dismiss = {
        if (!state.isSending) {
            if (!state.isSent && (state.message.isNotBlank() || state.email.isNotBlank())) confirmDiscard = true
            else close()
        }
    }
    Dialog(onDismissRequest = dismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            modifier = Modifier.fillMaxSize().imePadding(),
            topBar = {
                TopAppBar(title = { Text(stringResource(Res.string.feedback_title)) }, navigationIcon = {
                    IconButton(onClick = dismiss, enabled = !state.isSending) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.action_back))
                    }
                })
            },
            bottomBar = {
                Surface(shadowElevation = 3.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        state.error?.let { FeedbackSubmissionError(it) }
                        Button(
                            onClick = { if (state.isSent) close() else state.eventSink(FeedbackEvent.Submit) },
                            enabled = state.isSent || state.canSubmit,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(when {
                                state.isSent -> Res.string.action_done
                                state.isSending -> Res.string.feedback_sending
                                state.error != null -> Res.string.action_retry
                                else -> Res.string.feedback_send
                            }))
                        }
                    }
                }
            },
        ) { padding ->
            Column(
                Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.isSent) {
                    Text(stringResource(Res.string.feedback_thanks), style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(Res.string.feedback_sent))
                } else {
                    Text(stringResource(Res.string.feedback_intro))
                    OutlinedTextField(
                        value = state.message, onValueChange = { state.eventSink(FeedbackEvent.MessageChanged(it)) },
                        enabled = state.isEditable, minLines = 5, modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.feedback_message)) },
                        supportingText = { Text(stringResource(Res.string.feedback_length, state.validation.messageLength)) },
                        isError = state.validation.messageTooLong,
                    )
                    OutlinedTextField(
                        value = state.email, onValueChange = { state.eventSink(FeedbackEvent.EmailChanged(it)) },
                        enabled = state.isEditable, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.feedback_email)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = !state.validation.emailValid,
                        supportingText = {
                            if (!state.validation.emailValid) Text(stringResource(Res.string.feedback_email_invalid))
                        },
                    )
                    Text(stringResource(Res.string.feedback_privacy), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
    if (confirmDiscard) AlertDialog(
        onDismissRequest = { confirmDiscard = false },
        title = { Text(stringResource(Res.string.feedback_discard_title)) },
        confirmButton = { TextButton(onClick = { confirmDiscard = false; close() }) {
            Text(stringResource(Res.string.feedback_discard))
        } },
        dismissButton = { TextButton(onClick = { confirmDiscard = false }) {
            Text(stringResource(Res.string.feedback_keep))
        } },
    )
}

@Composable
private fun FeedbackSubmissionError(error: AppError) {
    Surface(
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {
            liveRegion = LiveRegionMode.Polite
        },
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(Res.string.feedback_send_error_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(error.localizedMessage(), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
