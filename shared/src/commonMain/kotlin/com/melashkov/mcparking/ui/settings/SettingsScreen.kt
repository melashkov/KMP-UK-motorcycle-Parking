package com.melashkov.mcparking.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.melashkov.mcparking.ui.theme.ThemeMode
import org.jetbrains.compose.resources.stringResource
import ukmotorcycleparking.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    themeMode: ThemeMode,
    localBackend: Boolean?,
    onThemeSelected: (ThemeMode) -> Unit,
    onBackendSelected: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.settings_title)) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.action_back))
                        }
                    },
                )
            },
        ) { padding ->
            Column(
                Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(24.dp),
            ) {
                Text(stringResource(Res.string.appearance_title), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(Res.string.appearance_description), modifier = Modifier.padding(top = 8.dp, bottom = 12.dp))
                Column(Modifier.selectableGroup()) {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                .selectable(themeMode == mode, role = Role.RadioButton, onClick = { onThemeSelected(mode) })
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = themeMode == mode, onClick = null)
                            Text(
                                stringResource(when (mode) {
                                    ThemeMode.SYSTEM -> Res.string.theme_system
                                    ThemeMode.LIGHT -> Res.string.theme_light
                                    ThemeMode.DARK -> Res.string.theme_dark
                                }),
                                modifier = Modifier.padding(start = 12.dp),
                            )
                        }
                    }
                }
                if (localBackend != null) {
                    DebugApiSettings(localBackend, onBackendSelected)
                }
            }
        }
    }
}

@Composable
private fun DebugApiSettings(
    localBackend: Boolean,
    onBackendSelected: (Boolean) -> Unit,
) {
    HorizontalDivider(Modifier.padding(vertical = 24.dp))
    Text(stringResource(Res.string.debug_api_title), style = MaterialTheme.typography.titleLarge)
    Column(Modifier.selectableGroup()) {
        listOf(false, true).forEach { local ->
            Row(
                Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    .selectable(localBackend == local, role = Role.RadioButton, onClick = { onBackendSelected(local) })
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = localBackend == local, onClick = null)
                Text(
                    debugApiLabel(local),
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun debugApiLabel(local: Boolean): String =
    stringResource(if (local) Res.string.debug_api_local else Res.string.debug_api_production)
