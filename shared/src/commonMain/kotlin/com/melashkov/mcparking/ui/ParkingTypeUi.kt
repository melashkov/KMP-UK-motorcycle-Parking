package com.melashkov.mcparking.ui

import androidx.compose.ui.graphics.Color
import com.melashkov.mcparking.domain.entity.ParkingType

val ParkingType.color: Color
    get() = when (this) {
        ParkingType.FREE -> Color.Blue
        ParkingType.PAY -> Color.Red
        ParkingType.PERMIT -> Color.Green
        ParkingType.UNCATEGORISED -> Color(0xFFFFA500) // Orange
        ParkingType.UNVERIFIED -> Color.Gray
        ParkingType.INACTIVE -> Color.Gray
    }
