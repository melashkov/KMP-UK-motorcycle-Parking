package com.melashkov.mcparking.ui.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

@Composable
internal actual fun rememberExternalActionHandler(): ExternalActionHandler {
    val context = LocalContext.current
    return remember(context) { AndroidExternalActionHandler(context) }
}

private class AndroidExternalActionHandler(
    private val context: Context,
) : ExternalActionHandler {

    override fun handle(action: ExternalAction) {
        when (action) {
            is ExternalAction.Directions -> openDirections(action)
            is ExternalAction.ShareText -> shareText(action)
            is ExternalAction.OpenUri -> context.startActivity(
                Intent(Intent.ACTION_VIEW, action.uri.toUri()),
            )
        }
    }

    private fun openDirections(action: ExternalAction.Directions) {
        val coordinates = "${action.latitude},${action.longitude}"
        val label = Uri.encode(action.label)

        val genericMapIntent = Intent(
            Intent.ACTION_VIEW,
            "geo:0,0?q=$coordinates($label)".toUri(),
        )
        val googleMapsIntent = Intent(
            Intent.ACTION_VIEW,
            "google.navigation:q=$coordinates&mode=d".toUri(),
        ).setPackage("com.google.android.apps.maps")
        val wazeIntent = Intent(
            Intent.ACTION_VIEW,
            ("https://waze.com/ul?ll=$coordinates" +
                "&navigate=yes&vehicle_type=motorcycle").toUri(),
        ).setPackage("com.waze")

        val chooser = Intent.createChooser(
            genericMapIntent,
            "Navigate with",
        ).apply {
            putExtra(
                Intent.EXTRA_INITIAL_INTENTS,
                arrayOf(wazeIntent, googleMapsIntent),
            )
        }
        context.startActivity(chooser)
    }

    private fun shareText(action: ExternalAction.ShareText) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, action.text)
        }
        context.startActivity(
            Intent.createChooser(shareIntent, action.chooserTitle),
        )
    }
}
