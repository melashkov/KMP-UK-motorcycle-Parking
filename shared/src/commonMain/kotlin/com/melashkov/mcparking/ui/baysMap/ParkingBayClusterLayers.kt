package com.melashkov.mcparking.ui.baysMap

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.maplibre.compose.expressions.dsl.asNumber
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.step
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.sources.GeoJsonSource
import org.maplibre.compose.util.ClickResult
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

@Composable
@MaplibreComposable
internal fun ParkingBayClusterLayers(
    source: GeoJsonSource,
    onClusterClick: ((Position) -> Unit)?,
) {
    val pointCount = feature["point_count"].asNumber()

    CircleLayer(
        id = "parking-bay-clusters",
        source = source,
        filter = feature.has("point_count"),
        radius = step(pointCount, const(14.dp), 25 to const(20.dp), 100 to const(28.dp)),
        color = step(
            pointCount,
            const(Color(0xFF4FC3F7)),
            25 to const(Color(0xFFFFB74D)),
            100 to const(Color(0xFFE57373)),
        ),
        opacity = const(0.85f),
        strokeWidth = const(1.dp),
        strokeColor = const(Color.White),
        onClick = { features ->
            val point = features.firstOrNull()?.geometry as? Point
            if (point != null && onClusterClick != null) {
                onClusterClick(point.coordinates)
                ClickResult.Consume
            } else {
                ClickResult.Pass
            }
        },
    )

    SymbolLayer(
        id = "parking-bay-cluster-counts",
        source = source,
        filter = feature.has("point_count"),
        textField = feature["point_count_abbreviated"].asString(),
        textColor = const(Color.Black),
    )
}
