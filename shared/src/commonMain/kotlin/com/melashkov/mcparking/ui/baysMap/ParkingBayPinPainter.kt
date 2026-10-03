package com.melashkov.mcparking.ui.baysMap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import com.melashkov.mcparking.domain.entity.ParkingType
import org.jetbrains.compose.resources.painterResource
import ukmotorcycleparking.shared.generated.resources.Res
import ukmotorcycleparking.shared.generated.resources.parking_pin_symbol
import ukmotorcycleparking.shared.generated.resources.parking_pin_background
import ukmotorcycleparking.shared.generated.resources.parking_pin_fill

@Composable
internal fun rememberParkingBayPinPainter(type: ParkingType): Painter {
    val backgroundPainter = painterResource(Res.drawable.parking_pin_background)
    val fillPainter = painterResource(Res.drawable.parking_pin_fill)
    val detailPainter = painterResource(Res.drawable.parking_pin_symbol)
    val color = type.parkingBayColor

    return remember(backgroundPainter, fillPainter, detailPainter, color) {
        ParkingPinPainter(
            backgroundPainter = backgroundPainter,
            fillPainter = fillPainter,
            detailPainter = detailPainter,
            color = color,
        )
    }
}

internal val ParkingType.parkingBayColor: Color
    get() =
        when (this) {
            ParkingType.FREE -> Color(0xFF357BC0)
            ParkingType.PAY -> Color(0xFFA94F4F)
            ParkingType.PERMIT -> Color(0xFF388E3C)
            ParkingType.UNCATEGORISED -> Color(0xFFA65D00)
            ParkingType.UNVERIFIED,
            ParkingType.INACTIVE -> Color(0xFF30343B)
        }

private class ParkingPinPainter(
    private val backgroundPainter: Painter,
    private val fillPainter: Painter,
    private val detailPainter: Painter,
    private val color: Color,
) : Painter() {
    override val intrinsicSize: Size
        get() = detailPainter.intrinsicSize

    override fun DrawScope.onDraw() {
        with(backgroundPainter) {
            draw(size = size)
        }
        with(fillPainter) {
            draw(size = size, colorFilter = ColorFilter.tint(color))
        }
        with(detailPainter) {
            draw(size = size, colorFilter = ColorFilter.tint(Color.White))
        }
    }
}
