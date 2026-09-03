package com.melashkov.mcparking.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.melashkov.mcparking.domain.entity.ParkingType
import org.jetbrains.compose.resources.stringResource
import ukmotorcycleparking.shared.generated.resources.Res
import ukmotorcycleparking.shared.generated.resources.parking_type_free
import ukmotorcycleparking.shared.generated.resources.parking_type_inactive
import ukmotorcycleparking.shared.generated.resources.parking_type_paid
import ukmotorcycleparking.shared.generated.resources.parking_type_permit_required
import ukmotorcycleparking.shared.generated.resources.parking_type_unclassified
import ukmotorcycleparking.shared.generated.resources.parking_type_unverified

val ParkingType.color: Color
    get() = when (this) {
        ParkingType.FREE -> Color.Blue
        ParkingType.PAY -> Color.Red
        ParkingType.PERMIT -> Color.Green
        ParkingType.UNCATEGORISED -> Color(0xFFFFA500) // Orange
        ParkingType.UNVERIFIED -> Color.Gray
        ParkingType.INACTIVE -> Color.Gray
    }

@Composable
fun ParkingType.localizedLabel(): String =
    stringResource(
        when (this) {
            ParkingType.FREE -> Res.string.parking_type_free
            ParkingType.PAY -> Res.string.parking_type_paid
            ParkingType.PERMIT -> Res.string.parking_type_permit_required
            ParkingType.UNCATEGORISED -> Res.string.parking_type_unclassified
            ParkingType.UNVERIFIED -> Res.string.parking_type_unverified
            ParkingType.INACTIVE -> Res.string.parking_type_inactive
        },
    )
