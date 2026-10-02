package com.melashkov.mcparking.ui.support

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import ukmotorcycleparking.shared.generated.resources.*

@Composable
internal fun MapOverflowMenu(
    onAddBay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val preferences = rememberDeveloperMessagePreferences()
    var page by rememberSaveable {
        mutableStateOf(if (preferences.lastSeenRelease != CurrentReleaseId) "news" else "")
    }
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    val dismiss = {
        if (page == "news") preferences.lastSeenRelease = CurrentReleaseId
        page = ""
    }

    Box(modifier) {
        SmallFloatingActionButton(onClick = { menuExpanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(Res.string.accessibility_map_menu))
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.action_add_parking_bay)) },
                leadingIcon = { Icon(Icons.Default.AddLocationAlt, contentDescription = null) },
                onClick = { menuExpanded = false; onAddBay() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.whats_new)) },
                onClick = { menuExpanded = false; page = "news" },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.action_developer_message)) },
                onClick = { menuExpanded = false; page = "story" },
            )
        }
    }

    if (page.isNotEmpty()) {
        ProjectInfoScreen(
            showReleaseNotes = page == "news",
            onDismiss = dismiss,
            onAddBay = { dismiss(); onAddBay() },
        )
    }
}

// Change only when publishing new release notes, not for every build.
internal const val CurrentReleaseId = "openstreetmap-redesign-1"
