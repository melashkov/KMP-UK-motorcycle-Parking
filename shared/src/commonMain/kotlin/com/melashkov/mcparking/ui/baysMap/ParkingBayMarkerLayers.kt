package com.melashkov.mcparking.ui.baysMap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.melashkov.mcparking.domain.entity.ParkingBay
import com.melashkov.mcparking.domain.entity.ParkingType
import kotlinx.collections.immutable.ImmutableList
import org.maplibre.compose.expressions.dsl.and
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.eq
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.not
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.sources.GeoJsonSource
import org.maplibre.compose.util.ClickResult
import org.maplibre.compose.util.MaplibreComposable

@Composable
@MaplibreComposable
internal fun ParkingBayMarkerLayers(
    markers: ImmutableList<ParkingBay>,
    source: GeoJsonSource,
    onMarkerClick: ((ParkingBay) -> Unit)?,
) {
    markers.forEach { marker ->
            LocationPinMarkerLayer(
                marker = marker,
                source = source,
                onMarkerClick = onMarkerClick,
            )
    }
}

@Composable
@MaplibreComposable
private fun LocationPinMarkerLayer(
    marker: ParkingBay,
    source: GeoJsonSource,
    onMarkerClick: ((ParkingBay) -> Unit)?,
) {
    CircleLayer(
        id = "parking-bay-marker-${marker.id}",
        source = source,
        filter = !feature.has("point_count") and
            (feature[PARKING_BAY_FEATURE_ID].asString() eq const(marker.id)),
        radius = const(7.dp),
        color = const(marker.color),
        strokeColor = const(Color.White),
        strokeWidth = const(2.dp),
        onClick = {
            if (onMarkerClick != null) {
                onMarkerClick(marker)
                ClickResult.Consume
            } else {
                ClickResult.Pass
            }
        },
    )
}

private val ParkingBay.color: Color
    get() =
        when (type) {
            ParkingType.FREE -> Color(0xFF1976D2)
            ParkingType.PAY -> Color(0xFFD32F2F)
            ParkingType.PERMIT -> Color(0xFF388E3C)
            ParkingType.UNCATEGORISED,
            ParkingType.UNVERIFIED -> Color(0xFFF57C00)
            ParkingType.INACTIVE -> Color(0xFF757575)
        }
