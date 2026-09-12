package com.melashkov.mcparking.ui.baysMap

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Streetview
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.melashkov.mcparking.domain.entity.ParkingBay
import com.melashkov.mcparking.domain.entity.ParkingType
import com.melashkov.mcparking.ui.localizedLabel
import org.jetbrains.compose.resources.stringResource
import ukmotorcycleparking.shared.generated.resources.Res
import ukmotorcycleparking.shared.generated.resources.action_navigate
import ukmotorcycleparking.shared.generated.resources.action_share
import ukmotorcycleparking.shared.generated.resources.action_street_view
import ukmotorcycleparking.shared.generated.resources.action_suggest_edit
import ukmotorcycleparking.shared.generated.resources.bay_details_fallback_name
import ukmotorcycleparking.shared.generated.resources.bay_details_section

@Composable
internal fun ParkingBayDetailsSheetContent(
    bay: ParkingBay,
    onNavigate: () -> Unit,
    onShare: () -> Unit,
    onSuggestEdit: () -> Unit,
    onStreetView: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = rememberParkingBayPinPainter(bay.type),
                contentDescription = null,
                modifier = Modifier.size(width = 40.dp, height = 50.dp),
            )

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = bay.title.ifBlank {
                        stringResource(Res.string.bay_details_fallback_name)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                ParkingTypeBadge(bay.type)
            }
        }

        val hasDescription = bay.description.isNotBlank()

        if (hasDescription) {
            SubtleDivider()
            DetailItem(
                label = stringResource(Res.string.bay_details_section),
                value = bay.description,
            )
        }

        SubtleDivider()

        ParkingBayActionButtons(
            onNavigate = onNavigate,
            onShare = onShare,
            onSuggestEdit = onSuggestEdit,
            onStreetView = onStreetView,
        )
    }
}

@Composable
private fun SubtleDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 20.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
    )
}

@Composable
private fun ParkingBayActionButtons(
    onNavigate: () -> Unit,
    onShare: () -> Unit,
    onSuggestEdit: () -> Unit,
    onStreetView: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ParkingBayActionButton(
                label = stringResource(Res.string.action_navigate),
                icon = Icons.Default.Directions,
                onClick = onNavigate,
                modifier = Modifier.weight(1f),
            )
            ParkingBayActionButton(
                label = stringResource(Res.string.action_share),
                icon = Icons.Default.Share,
                onClick = onShare,
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ParkingBayActionButton(
                label = stringResource(Res.string.action_suggest_edit),
                icon = Icons.Default.Edit,
                onClick = onSuggestEdit,
                modifier = Modifier.weight(1f),
            )
            ParkingBayActionButton(
                label = stringResource(Res.string.action_street_view),
                icon = Icons.Default.Streetview,
                onClick = onStreetView,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ParkingBayActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
private fun ParkingTypeBadge(type: ParkingType) {
    val color = type.parkingBayColor

    Surface(
        color = color.copy(alpha = 0.14f),
        shape = CircleShape,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Text(
                text = type.localizedLabel(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
