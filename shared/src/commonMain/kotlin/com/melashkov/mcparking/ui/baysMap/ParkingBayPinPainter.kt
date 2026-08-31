package com.melashkov.mcparking.ui.baysMap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import com.melashkov.mcparking.domain.entity.ParkingType

@Composable
internal fun rememberParkingBayPinPainter(type: ParkingType): Painter =
    remember(type) {
        ParkingBayPinPainter(type.parkingBayColor)
    }

internal val ParkingType.parkingBayColor: Color
    get() =
        when (this) {
            ParkingType.FREE -> Color(0xFF1976D2)
            ParkingType.PAY -> Color(0xFFD32F2F)
            ParkingType.PERMIT -> Color(0xFF388E3C)
            ParkingType.UNCATEGORISED,
            ParkingType.UNVERIFIED -> Color(0xFFF57C00)
            ParkingType.INACTIVE -> Color(0xFF757575)
        }

private class ParkingBayPinPainter(
    private val color: Color,
) : Painter() {
    override val intrinsicSize: Size = Size(width = 48f, height = 60f)

    override fun DrawScope.onDraw() {
        val width = size.width
        val height = size.height
        val shell = pinPath(width, height)
        val lightColor = lerp(color, Color.White, 0.34f)
        val deepColor = lerp(color, Color.Black, 0.42f)

        drawPath(
            path = shell,
            brush = Brush.linearGradient(
                colors = listOf(lightColor, color, deepColor),
                start = Offset(width * 0.16f, height * 0.04f),
                end = Offset(width * 0.82f, height * 0.92f),
            ),
        )
        drawPath(
            path = shell,
            color = Color.White,
            style = Stroke(width = width * 0.05f, join = StrokeJoin.Round),
        )

        drawRecessedBadge(width, height, lightColor, deepColor)
        drawParkingLetter(width, height)
        drawMotorcycle(width, height)
        drawShellHighlight(width, height)
    }

    private fun DrawScope.drawRecessedBadge(
        width: Float,
        height: Float,
        lightColor: Color,
        deepColor: Color,
    ) {
        val outerTopLeft = Offset(width * 0.21f, height * 0.105f)
        val outerSize = Size(width * 0.58f, height * 0.46f)
        val innerTopLeft = Offset(width * 0.235f, height * 0.13f)
        val innerSize = Size(width * 0.53f, height * 0.405f)

        drawOval(
            color = deepColor.copy(alpha = 0.72f),
            topLeft = outerTopLeft,
            size = outerSize,
        )
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(lightColor, color),
                center = Offset(width * 0.38f, height * 0.22f),
                radius = width * 0.43f,
            ),
            topLeft = innerTopLeft,
            size = innerSize,
        )
        drawOval(
            color = Color.White.copy(alpha = 0.30f),
            topLeft = innerTopLeft,
            size = innerSize,
            style = Stroke(width = width * 0.018f),
        )
    }

    private fun DrawScope.drawParkingLetter(width: Float, height: Float) {
        val parkingPath = Path().apply {
            moveTo(width * 0.33f, height * 0.17f)
            lineTo(width * 0.55f, height * 0.17f)
            cubicTo(
                width * 0.67f,
                height * 0.17f,
                width * 0.72f,
                height * 0.225f,
                width * 0.72f,
                height * 0.29f,
            )
            cubicTo(
                width * 0.72f,
                height * 0.36f,
                width * 0.66f,
                height * 0.40f,
                width * 0.55f,
                height * 0.40f,
            )
            lineTo(width * 0.43f, height * 0.40f)
            lineTo(width * 0.43f, height * 0.49f)
            lineTo(width * 0.33f, height * 0.49f)
            close()
        }
        val counterPath = Path().apply {
            moveTo(width * 0.43f, height * 0.245f)
            lineTo(width * 0.55f, height * 0.245f)
            cubicTo(
                width * 0.60f,
                height * 0.245f,
                width * 0.63f,
                height * 0.265f,
                width * 0.63f,
                height * 0.29f,
            )
            cubicTo(
                width * 0.63f,
                height * 0.315f,
                width * 0.60f,
                height * 0.33f,
                width * 0.55f,
                height * 0.33f,
            )
            lineTo(width * 0.43f, height * 0.33f)
            close()
        }

        drawPath(path = parkingPath, color = Color.White.copy(alpha = 0.62f))
        drawPath(path = counterPath, color = color.copy(alpha = 0.82f))
    }

    private fun DrawScope.drawMotorcycle(width: Float, height: Float) {
        val white = Color.White
        val rearWheel = Offset(width * 0.34f, height * 0.455f)
        val frontWheel = Offset(width * 0.67f, height * 0.455f)
        val engine = Offset(width * 0.49f, height * 0.44f)
        val seatPost = Offset(width * 0.43f, height * 0.365f)
        val steering = Offset(width * 0.60f, height * 0.355f)
        val wheelRadius = width * 0.078f
        val wheelStroke = width * 0.04f
        val frameStroke = width * 0.042f

        drawCircle(white, wheelRadius, rearWheel, style = Stroke(wheelStroke))
        drawCircle(white, wheelRadius, frontWheel, style = Stroke(wheelStroke))

        drawLine(white, rearWheel, engine, frameStroke, StrokeCap.Round)
        drawLine(white, engine, seatPost, frameStroke, StrokeCap.Round)
        drawLine(white, seatPost, rearWheel, frameStroke, StrokeCap.Round)
        drawLine(white, seatPost, steering, frameStroke, StrokeCap.Round)
        drawLine(white, steering, frontWheel, frameStroke, StrokeCap.Round)

        val bodyPath = Path().apply {
            moveTo(width * 0.45f, height * 0.365f)
            cubicTo(
                width * 0.50f,
                height * 0.335f,
                width * 0.57f,
                height * 0.335f,
                width * 0.61f,
                height * 0.37f,
            )
            lineTo(width * 0.55f, height * 0.42f)
            lineTo(width * 0.45f, height * 0.405f)
            close()
        }
        drawPath(bodyPath, white)

        drawLine(
            color = white,
            start = Offset(width * 0.36f, height * 0.35f),
            end = Offset(width * 0.47f, height * 0.35f),
            strokeWidth = width * 0.06f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = white,
            start = steering,
            end = Offset(width * 0.62f, height * 0.305f),
            strokeWidth = frameStroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = white,
            start = Offset(width * 0.595f, height * 0.305f),
            end = Offset(width * 0.66f, height * 0.305f),
            strokeWidth = frameStroke,
            cap = StrokeCap.Round,
        )
    }

    private fun DrawScope.drawShellHighlight(width: Float, height: Float) {
        val highlight = Path().apply {
            moveTo(width * 0.28f, height * 0.105f)
            cubicTo(
                width * 0.34f,
                height * 0.065f,
                width * 0.42f,
                height * 0.055f,
                width * 0.49f,
                height * 0.055f,
            )
        }
        drawPath(
            path = highlight,
            color = Color.White.copy(alpha = 0.42f),
            style = Stroke(width * 0.045f, cap = StrokeCap.Round),
        )
    }
}

private fun pinPath(width: Float, height: Float): Path =
    Path().apply {
        moveTo(width * 0.50f, height * 0.975f)
        cubicTo(
            width * 0.43f,
            height * 0.84f,
            width * 0.12f,
            height * 0.60f,
            width * 0.12f,
            height * 0.34f,
        )
        cubicTo(
            width * 0.12f,
            height * 0.15f,
            width * 0.28f,
            height * 0.04f,
            width * 0.50f,
            height * 0.04f,
        )
        cubicTo(
            width * 0.72f,
            height * 0.04f,
            width * 0.88f,
            height * 0.15f,
            width * 0.88f,
            height * 0.34f,
        )
        cubicTo(
            width * 0.88f,
            height * 0.60f,
            width * 0.57f,
            height * 0.84f,
            width * 0.50f,
            height * 0.975f,
        )
        close()
    }
