package com.melashkov.mcparking.ui.baysMap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.melashkov.mcparking.domain.entity.ParkingBay
import com.melashkov.mcparking.domain.entity.ParkingType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.compose.expressions.dsl.and
import org.maplibre.compose.expressions.dsl.asNumber
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.eq
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.not
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.layers.SymbolLayer
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
    ParkingType.entries.forEach { type ->
        key(type) {
            ParkingBayTypeMarkerLayer(
                type = type,
                markers = markers,
                source = source,
                onMarkerClick = onMarkerClick,
            )
        }
    }
}

@Composable
@MaplibreComposable
private fun ParkingBayTypeMarkerLayer(
    type: ParkingType,
    markers: ImmutableList<ParkingBay>,
    source: GeoJsonSource,
    onMarkerClick: ((ParkingBay) -> Unit)?,
) {
    val pinPainter = rememberParkingBayPinPainter(type)

    SymbolLayer(
        id = "parking-bay-markers-${type.value}",
        source = source,
        filter = !feature.has("point_count") and
            (feature[PARKING_BAY_TYPE].asNumber() eq const(type.value)),
        iconImage = image(
            value = pinPainter,
            size = DpSize(width = 32.dp, height = 40.dp),
        ),
        iconAnchor = const(SymbolAnchor.Bottom),
        iconAllowOverlap = const(true),
        iconIgnorePlacement = const(true),
        onClick = { features ->
            val markerId = features.firstOrNull()
                ?.properties
                ?.get(PARKING_BAY_FEATURE_ID)
                ?.jsonPrimitive
                ?.contentOrNull
            val marker = markers.firstOrNull { it.id == markerId }

            if (marker != null && onMarkerClick != null) {
                onMarkerClick(marker)
                ClickResult.Consume
            } else {
                ClickResult.Pass
            }
        },
    )
}
