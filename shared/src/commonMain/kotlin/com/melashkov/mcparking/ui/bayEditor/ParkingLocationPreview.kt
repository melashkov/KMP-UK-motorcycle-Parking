package com.melashkov.mcparking.ui.bayEditor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.ParkingType
import com.melashkov.mcparking.ui.shared.ParkingMapOverlay
import com.melashkov.mcparking.ui.theme.parkingMapStyle
import org.jetbrains.compose.resources.stringResource
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.interaction.MapInteractions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.spatialk.geojson.Position
import ukmotorcycleparking.shared.generated.resources.Res
import ukmotorcycleparking.shared.generated.resources.action_change

@Composable
internal fun ParkingLocationPreview(
    location: GeoCoordinate,
    type: ParkingType?,
    error: String?,
    onChooseLocation: () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)

    Surface(
        shape = shape,
        border = BorderStroke(
            width = 1.dp,
            color = if (error == null) {
                MaterialTheme.colorScheme.outlineVariant
            } else {
                MaterialTheme.colorScheme.error
            },
        ),
    ) {
        key(location) {
            val cameraState = rememberMapState(
                baseStyle = parkingMapStyle(),
                initialCameraPosition = CameraPosition(
                    center = Position(
                        longitude = location.longitude,
                        latitude = location.latitude,
                    ),
                    zoom = LocationPreviewZoom,
                ),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(184.dp)
                    .clip(shape),
            ) {
                MaplibreMap(
                    modifier = Modifier.matchParentSize(),
                    state = cameraState,
                    overlay = {
                        ParkingMapOverlay(cameraState, contentWindowInsets = WindowInsets(0, 0, 0, 0))
                    },
                    interactions = MapInteractions(MapInteractions.None) {
                        callbacks {
                            click {
                                onEvent {
                                    onChooseLocation()
                                    ClickResult.Consume
                                }
                            }
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

                Button(
                    onClick = onChooseLocation,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                ) {
                    Icon(Icons.Default.EditLocationAlt, contentDescription = null)
                    Text(
                        text = stringResource(Res.string.action_change),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }

    error?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp),
        )
    }
}

private const val LocationPreviewZoom = 16.0
