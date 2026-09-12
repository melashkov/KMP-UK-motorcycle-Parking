package com.melashkov.mcparking.ui.bayEditor

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melashkov.mcparking.domain.entity.ParkingType
import com.melashkov.mcparking.ui.baysMap.rememberParkingBayPinPainter

@Composable
internal fun ParkingLocationPin(
    type: ParkingType?,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = rememberParkingBayPinPainter(type ?: ParkingType.UNCATEGORISED),
        contentDescription = null,
        tint = androidx.compose.ui.graphics.Color.Unspecified,
        modifier = modifier,
    )
}

internal val ParkingPinWidth = 48.dp
internal val ParkingPinHeight = 60.dp
