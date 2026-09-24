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

/**
 * Renders parking bays on a MapLibre map with clustering support.
 *
 * Builds one GeoJSON feature collection from the immutable marker list, creates one clustered
 * source, then renders marker and cluster layers from that source.
 */
@Composable
@MaplibreComposable
fun ParkingBayMarkers(
    markers: ImmutableList<ParkingBay>,
    onMarkerClick: ((ParkingBay) -> Unit)? = null,
    onClusterClick: ((Position) -> Unit)? = null,
) {
    if (markers.isEmpty()) return

    val geoJsonData = remember(markers) {
        GeoJsonData.Features(markers.toFeatureCollection())
    }

    val markersSource = rememberGeoJsonSource(
        data = geoJsonData,
        options = GeoJsonOptions(
            cluster = true,
            clusterMinPoints = 2,
            clusterRadius = 40,
            clusterMaxZoom = 14,
        ),
    )

    ParkingBayMarkerLayers(
        markers = markers,
        source = markersSource,
        onMarkerClick = onMarkerClick,
    )
    ParkingBayClusterLayers(
        source = markersSource,
        onClusterClick = onClusterClick,
    )
}
