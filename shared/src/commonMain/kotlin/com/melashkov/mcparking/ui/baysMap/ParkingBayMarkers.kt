package com.melashkov.mcparking.ui.baysMap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.melashkov.mcparking.domain.entity.ParkingBay
import kotlinx.collections.immutable.ImmutableList
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.toJson

/**
 * Renders parking bays on a MapLibre map with clustering support.
 *
 * This follows the LocationPinMarkers source shape: build one GeoJSON string from an immutable
 * marker list, create one clustered source, then render cluster layers and individual marker
 * layers from that source. Individual parking bays are CircleLayers, not bitmap SymbolLayers.
 */
@Composable
@MaplibreComposable
fun ParkingBayMarkers(
    markers: ImmutableList<ParkingBay>,
    onMarkerClick: ((ParkingBay) -> Unit)? = null,
    onClusterClick: ((Position) -> Unit)? = null,
) {
    if (markers.isEmpty()) return

    val geoJsonString = remember(markers) {
        markers.toFeatureCollection().toJson()
    }

    val markersSource = rememberGeoJsonSource(
        data = GeoJsonData.JsonString(geoJsonString),
        options = GeoJsonOptions(
            cluster = true,
            clusterMinPoints = 3,
            clusterRadius = 30,
            //clusterMaxZoom = 14,
            //synchronousUpdate = true,
        ),
    )

    ParkingBayClusterLayers(
        source = markersSource,
        onClusterClick = onClusterClick,
    )
    ParkingBayMarkerLayers(
        markers = markers,
        source = markersSource,
        onMarkerClick = onMarkerClick,
    )
}
