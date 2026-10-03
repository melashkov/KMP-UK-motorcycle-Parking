package com.melashkov.mcparking.ui.support

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.compose.resources.stringResource
import ukmotorcycleparking.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProjectInfoScreen(
    showReleaseNotes: Boolean,
    onDismiss: () -> Unit,
    onAddBay: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    var linkFailed by remember { mutableStateOf(false) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(if (showReleaseNotes) Res.string.whats_new else Res.string.action_developer_message))
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.action_back))
                        }
                    },
                )
            },
            bottomBar = {
                Surface {
                    androidx.compose.foundation.layout.Box(
                        Modifier.fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(24.dp),
                    ) {
                        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(Res.string.action_continue_to_map))
                        }
                    }
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier.padding(padding).fillMaxSize()
                    .verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                if (showReleaseNotes) {
                    Text(stringResource(Res.string.release_title), style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(Res.string.release_map_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(Res.string.release_map_body))
                    Text(stringResource(Res.string.release_search_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(Res.string.release_search_body))
                } else {
                    Text(stringResource(Res.string.developer_message_history))
                    Text(stringResource(Res.string.developer_message_thanks))
                    Text(stringResource(Res.string.support_contribute))
                    OutlinedButton(onClick = onAddBay, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(Res.string.action_add_parking_bay))
                    }
                }
                if (SupportConfig.koFiUrl.isNotBlank()) {
                    Text(stringResource(Res.string.support_ko_fi_description))
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF2B85B),
                            contentColor = Color(0xFF302315),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            linkFailed = runCatching { uriHandler.openUri(SupportConfig.koFiUrl) }.isFailure
                        },
                    ) { Text(stringResource(Res.string.support_ko_fi)) }
                    if (linkFailed) Text(stringResource(Res.string.support_link_failed, SupportConfig.koFiUrl))
                }
            }
        }
    }
}
