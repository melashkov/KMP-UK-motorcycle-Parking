package com.melashkov.mcparking.ui.navigation

sealed interface ExternalAction {
    data class Directions(
        val latitude: Double,
        val longitude: Double,
        val label: String,
    ) : ExternalAction

    data class ShareText(
        val text: String,
        val chooserTitle: String,
    ) : ExternalAction

    data class OpenUri(val uri: String) : ExternalAction
}
