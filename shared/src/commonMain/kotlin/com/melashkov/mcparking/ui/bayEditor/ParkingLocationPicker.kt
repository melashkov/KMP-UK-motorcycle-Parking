package com.melashkov.mcparking.ui.bayEditor

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.ParkingType
import com.melashkov.mcparking.ui.shared.ParkingMapOverlay
import com.melashkov.mcparking.ui.theme.parkingMapStyle
import kotlin.math.roundToLong
import org.jetbrains.compose.resources.stringResource
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.interaction.MapInteractions
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.spatialk.geojson.Position
import ukmotorcycleparking.shared.generated.resources.Res
import ukmotorcycleparking.shared.generated.resources.action_back
import ukmotorcycleparking.shared.generated.resources.action_use_this_location
import ukmotorcycleparking.shared.generated.resources.location_picker_instruction
import ukmotorcycleparking.shared.generated.resources.location_picker_title

@Composable
internal fun ParkingLocationPicker(
    initialLocation: GeoCoordinate,
    type: ParkingType?,
    onBack: () -> Unit,
    onLocationSelected: (GeoCoordinate) -> Unit,
) {
    val cameraState = rememberMapState(
        baseStyle = parkingMapStyle(),
        initialCameraPosition = CameraPosition(
            center = Position(
                longitude = initialLocation.longitude,
                latitude = initialLocation.latitude,
            ),
            zoom = LocationPickerZoom,
        ),
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.location_picker_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.action_back),
                        )
                    }
                },
            )
        },
        bottomBar = {
            LocationPickerBottomBar(
                cameraState = cameraState,
                onLocationSelected = onLocationSelected,
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                state = cameraState,
                overlay = {
                    ParkingMapOverlay(cameraState, contentWindowInsets = WindowInsets(0, 0, 0, 0))
                },
                interactions = MapInteractions {
                    camera {
                        rotate { enabled = false }
                        pitch { enabled = false }
                    }
                },
            )

            ParkingLocationPin(
                type = type,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = -(ParkingPinHeight / 2))
                    .size(ParkingPinWidth, ParkingPinHeight),
            )

            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                ),
            ) {
                Text(
                    text = stringResource(Res.string.location_picker_instruction),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun LocationPickerBottomBar(
    cameraState: MapState,
    onLocationSelected: (GeoCoordinate) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        LocationCoordinates(cameraState)

        Button(
            onClick = {
                val target = cameraState.cameraPosition.center
                onLocationSelected(
                    GeoCoordinate(
                        latitude = target.latitude,
                        longitude = target.longitude,
                    ),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Text(
                text = stringResource(Res.string.action_use_this_location),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun LocationCoordinates(cameraState: MapState) {
    val coordinateText by remember(cameraState) {
        derivedStateOf {
            val target = cameraState.cameraPosition.center
            "${target.latitude.shortCoordinate}, ${target.longitude.shortCoordinate}"
        }
    }

    Text(
        text = coordinateText,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

private val Double.shortCoordinate: String
    get() = ((this * 100_000.0).roundToLong() / 100_000.0).toString()

private const val LocationPickerZoom = 17.0
