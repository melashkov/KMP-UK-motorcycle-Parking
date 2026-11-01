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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
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
    onSendFeedback: () -> Unit,
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
                    Text(stringResource(Res.string.release_search_body))
                    Text(stringResource(Res.string.release_feedback_body))
                } else {
                    Text(stringResource(Res.string.developer_message_history))
                    Text(stringResource(Res.string.developer_message_thanks))
                    Text(stringResource(Res.string.support_contribute))
                    OutlinedButton(onClick = onAddBay, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(Res.string.action_add_parking_bay))
                    }
                }
                OutlinedButton(onClick = onSendFeedback, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.feedback_title))
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
                if (!showReleaseNotes) {
                    CreditText(
                        stringResource(Res.string.developer_message_openstreetmap),
                        mapOf("OpenStreetMap contributors" to "https://www.openstreetmap.org/copyright"),
                    )
                    CreditText(
                        stringResource(Res.string.developer_message_openfreemap),
                        mapOf(
                            "Zsolt Ero (hyperknot)" to "https://github.com/hyperknot",
                            "OpenFreeMap" to "https://openfreemap.org/",
                        ),
                    )
                    CreditText(
                        stringResource(Res.string.developer_message_places),
                        mapOf(
                            "OS Open Names" to "https://docs.os.uk/os-downloads/products/addresses-and-names-portfolio/os-open-names",
                            "place names" to "https://admin.opendatani.gov.uk/dataset/osni-open-data-gazetteer-place-names",
                            "street names" to "https://admin.opendatani.gov.uk/dataset/osni-opendata-gazetteer-streetnames",
                        ),
                    )
                    Text(stringResource(Res.string.places_attribution), style = MaterialTheme.typography.bodySmall)
                    val licenceLabel = stringResource(Res.string.places_licence_link)
                    CreditText(
                        licenceLabel,
                        mapOf(licenceLabel to "https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/"),
                    )
                }
            }
        }
    }
}

@Composable
private fun CreditText(text: String, links: Map<String, String>) {
    val linkStyle = TextLinkStyles(
        style = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
        ),
    )
    Text(buildAnnotatedString {
        append(text)
        links.forEach { (label, url) ->
            val start = text.indexOf(label)
            if (start >= 0) {
                addLink(LinkAnnotation.Url(url, linkStyle), start, start + label.length)
            }
        }
    }, style = MaterialTheme.typography.bodySmall)
}
