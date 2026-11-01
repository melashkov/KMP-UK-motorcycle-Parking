package com.melashkov.mcparking.ui.baysMap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ukmotorcycleparking.shared.generated.resources.*

@Composable
internal fun PlacesDataCreditsDialog(onDismiss: () -> Unit) {
    var licenceLinkFailed by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val licenceUrl = "https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.places_data_credits)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(Res.string.places_attribution))
                Text(stringResource(Res.string.places_coverage))
                if (licenceLinkFailed) Text(stringResource(Res.string.support_link_failed, licenceUrl))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_done)) }
        },
        dismissButton = {
            TextButton(onClick = {
                licenceLinkFailed = runCatching { uriHandler.openUri(licenceUrl) }.isFailure
            }) { Text(stringResource(Res.string.places_licence_link)) }
        },
    )
}
