package com.melashkov.mcparking.ui.baysMap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.melashkov.mcparking.domain.entity.Place
import kotlinx.serialization.json.JsonObject
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

internal fun Place.destinationZoom(): Double = when (type) {
    "city", "town", "village", "hamlet", "settlement", "suburb" -> 13.0
    else -> 15.0
}

@Composable
@MaplibreComposable
internal fun DestinationMarker(place: Place) {
    val data = remember(place) {
        GeoJsonData.Features(FeatureCollection(listOf(Feature(
            geometry = Point(Position(place.longitude, place.latitude)),
            properties = JsonObject(emptyMap()),
        ))))
    }
    val source = rememberGeoJsonSource(data = data)
    CircleLayer(id = "search-destination", source = source,
        radius = const(7.dp), color = const(Color(0xFF7C3AED)),
        strokeWidth = const(3.dp), strokeColor = const(Color.White))
}
