package com.melashkov.mcparking.ui.baysMap

import com.melashkov.mcparking.domain.entity.ParkingBay
import com.melashkov.mcparking.ui.navigation.ExternalAction

internal fun ParkingBay.directionsAction(): ExternalAction =
    ExternalAction.Directions(
        latitude = position.latitude,
        longitude = position.longitude,
        label = title.ifBlank { "Motorcycle parking" },
    )

internal fun ParkingBay.shareAction(): ExternalAction {
    val name = title.ifBlank { "Motorcycle parking" }
    val location = "https://www.google.com/maps/search/?api=1" +
        "&query=${position.latitude},${position.longitude}"

    return ExternalAction.ShareText(
        text = "$name\n$location",
        chooserTitle = "Share parking bay",
    )
}

internal fun ParkingBay.streetViewAction(): ExternalAction =
    ExternalAction.OpenUri(
        uri = "https://www.google.com/maps/@?api=1" +
            "&map_action=pano" +
            "&viewpoint=${position.latitude},${position.longitude}",
    )
