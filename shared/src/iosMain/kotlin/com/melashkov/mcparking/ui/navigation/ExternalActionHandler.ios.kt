package com.melashkov.mcparking.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

@Composable
internal actual fun rememberExternalActionHandler(): ExternalActionHandler {
    val uriHandler = LocalUriHandler.current
    return remember(uriHandler) { IosExternalActionHandler(uriHandler) }
}

private class IosExternalActionHandler(
    private val uriHandler: UriHandler,
) : ExternalActionHandler {

    override fun handle(action: ExternalAction) {
        when (action) {
            is ExternalAction.Directions -> uriHandler.openUri(
                "https://maps.apple.com/?daddr=${action.latitude},${action.longitude}&dirflg=d",
            )

            is ExternalAction.ShareText -> shareText(action.text)
            is ExternalAction.OpenUri -> uriHandler.openUri(action.uri)
        }
    }

    private fun shareText(text: String) {
        val presenter = UIApplication.sharedApplication.keyWindow
            ?.rootViewController
            ?.topmostViewController()
            ?: return

        val shareSheet = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null,
        )
        presenter.presentViewController(
            viewControllerToPresent = shareSheet,
            animated = true,
            completion = null,
        )
    }
}

private tailrec fun UIViewController.topmostViewController(): UIViewController =
    presentedViewController?.topmostViewController() ?: this
