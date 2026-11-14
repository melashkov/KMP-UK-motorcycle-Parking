package com.melashkov.mcparking.ui.theme

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.melashkov.mcparking.di.DebugApiBackend
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import ukmotorcycleparking.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(onDismiss: () -> Unit) {
    val settings = LocalThemeSettings.current
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
                    .padding(24.dp).selectableGroup(),
            ) {
                Text(stringResource(Res.string.appearance_title), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(Res.string.appearance_description), modifier = Modifier.padding(top = 8.dp, bottom = 12.dp))
                ThemeMode.entries.forEach { mode ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            .selectable(settings.mode == mode, role = Role.RadioButton, onClick = { settings.setMode(mode) })
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = settings.mode == mode, onClick = null)
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
                DebugApiSettings()
            }
        }
    }
}

@Composable
private fun DebugApiSettings() {
    val backend = koinInject<DebugApiBackend>()
    if (!backend.enabled) return
    val selection by backend.selection.collectAsState()
    val activeBackend = debugApiLabel(selection.activeLocal)
    HorizontalDivider(Modifier.padding(vertical = 24.dp))
    Text(stringResource(Res.string.debug_api_title), style = MaterialTheme.typography.titleLarge)
    Text(stringResource(Res.string.debug_api_active, activeBackend), modifier = Modifier.padding(top = 8.dp))
    if (selection.restartRequired) {
        Text(stringResource(Res.string.debug_api_restart_required), modifier = Modifier.padding(top = 8.dp))
    }
    Text(
        stringResource(Res.string.debug_api_next_launch),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 12.dp),
    )
    Column(Modifier.selectableGroup()) {
        listOf(false, true).forEach { local ->
            Row(
                Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    .selectable(selection.nextLaunchLocal == local, role = Role.RadioButton, onClick = {
                        backend.selectLocal(local)
                    }).padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = selection.nextLaunchLocal == local, onClick = null)
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
