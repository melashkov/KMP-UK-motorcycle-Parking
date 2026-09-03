package com.melashkov.mcparking.ui.bayEditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melashkov.mcparking.domain.entity.ParkingType
import com.melashkov.mcparking.ui.baysMap.parkingBayColor

@Composable
internal fun ParkingTypeSelector(
    mode: BayEditorMode,
    selected: ParkingType?,
    error: String?,
    onSelected: (ParkingType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Parking type",
            style = MaterialTheme.typography.titleSmall,
        )

        BayEditorViewModel.EditableParkingTypes.chunked(2).forEach { rowTypes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowTypes.forEach { type ->
                    FilterChip(
                        selected = selected == type,
                        onClick = { onSelected(type) },
                        label = { Text(type.editorLabel) },
                        leadingIcon = {
                            Box(
                                Modifier
                                    .size(10.dp)
                                    .background(type.parkingBayColor, CircleShape),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (mode == BayEditorMode.Edit) {
            FilterChip(
                selected = selected == ParkingType.INACTIVE,
                onClick = { onSelected(ParkingType.INACTIVE) },
                label = { Text("Inactive / no longer exists") },
                leadingIcon = {
                    Box(
                        Modifier
                            .size(10.dp)
                            .background(ParkingType.INACTIVE.parkingBayColor, CircleShape),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Choose inactive if the parking bay has been removed or can no longer be used.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private val ParkingType.editorLabel: String
    get() = when (this) {
        ParkingType.FREE -> "Free"
        ParkingType.PAY -> "Paid"
        ParkingType.PERMIT -> "Permit required"
        ParkingType.UNCATEGORISED -> "Unclassified"
        ParkingType.UNVERIFIED -> "Unverified"
        ParkingType.INACTIVE -> "Inactive / no longer exists"
    }
