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
import com.melashkov.mcparking.ui.localizedLabel
import org.jetbrains.compose.resources.stringResource
import ukmotorcycleparking.shared.generated.resources.Res
import ukmotorcycleparking.shared.generated.resources.parking_type_inactive_help
import ukmotorcycleparking.shared.generated.resources.parking_type_inactive_option
import ukmotorcycleparking.shared.generated.resources.parking_type_label

@Composable
internal fun ParkingTypeSelector(
    mode: BayEditorMode,
    selected: ParkingType?,
    error: BayEditorFieldError?,
    onSelected: (ParkingType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(Res.string.parking_type_label),
            style = MaterialTheme.typography.titleSmall,
        )

        BayEditorFormState.EditableParkingTypes.chunked(2).forEach { rowTypes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowTypes.forEach { type ->
                    FilterChip(
                        selected = selected == type,
                        onClick = { onSelected(type) },
                        label = { Text(type.localizedLabel()) },
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
                label = {
                    Text(stringResource(Res.string.parking_type_inactive_option))
                },
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
                text = stringResource(Res.string.parking_type_inactive_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        error?.let {
            Text(
                text = it.localizedEditorMessage(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
