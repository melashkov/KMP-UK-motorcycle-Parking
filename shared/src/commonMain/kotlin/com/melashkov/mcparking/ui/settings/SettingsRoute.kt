package com.melashkov.mcparking.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SettingsRoute(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val localBackend by viewModel.localBackend.collectAsStateWithLifecycle()
    SettingsScreen(
        themeMode = themeMode,
        localBackend = localBackend,
        onThemeSelected = viewModel::selectTheme,
        onBackendSelected = viewModel::selectBackend,
        onDismiss = onDismiss,
    )
}
