package com.melashkov.mcparking.ui.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import org.maplibre.compose.map.MapState
import org.maplibre.compose.overlay.DisappearingCompassButton
import org.maplibre.compose.overlay.DisappearingScaleBar

// Keep map controls clear of the persistent attribution footer.
internal val MapAttributionSpace = 40.dp

@Composable
internal fun ParkingMapOverlay(
    mapState: MapState,
    contentWindowInsets: WindowInsets = WindowInsets.safeDrawing,
) {
    Box(
        Modifier.fillMaxSize()
            .windowInsetsPadding(contentWindowInsets)
            .padding(8.dp),
    ) {
        DisappearingScaleBar(
            metersPerDp = { mapState.viewport?.metersPerDpAtCenter ?: 0.0 },
            zoom = { mapState.cameraPosition.zoom },
            modifier = Modifier.align(Alignment.TopStart),
        )
        DisappearingCompassButton(Modifier.align(Alignment.TopEnd))

        // Our OpenFreeMap tiles require OpenMapTiles and OpenStreetMap credits.
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter),
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            val linkStyles = TextLinkStyles(
                style = SpanStyle(textDecoration = TextDecoration.Underline),
            )
            Text(
                text = buildAnnotatedString {
                    withLink(LinkAnnotation.Url("https://openmaptiles.org/", linkStyles)) {
                        append("© OpenMapTiles")
                    }
                    append(" · ")
                    withLink(LinkAnnotation.Url("https://www.openstreetmap.org/copyright", linkStyles)) {
                        append("© OpenStreetMap contributors")
                    }
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}
