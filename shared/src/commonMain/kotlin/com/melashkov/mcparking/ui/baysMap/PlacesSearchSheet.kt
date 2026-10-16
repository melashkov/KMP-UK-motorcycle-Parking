package com.melashkov.mcparking.ui.baysMap

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.Place
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ukmotorcycleparking.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlacesSearchSheet(near: GeoCoordinate?, onSelect: (Place) -> Unit, onDismiss: () -> Unit) {
    val viewModel = koinViewModel<PlacesSearchViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focus = remember { FocusRequester() }
    val dismiss = { viewModel.reset(); onDismiss() }
    DisposableEffect(viewModel) { onDispose { viewModel.reset() } }
    ModalBottomSheet(onDismissRequest = dismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).imePadding()) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { viewModel.search(it, near) },
                label = { Text(stringResource(Res.string.places_search_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.search(state.query, near, immediate = true) }),
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
            )
            Spacer(Modifier.height(12.dp))
            Text(stringResource(Res.string.places_coverage), style = MaterialTheme.typography.bodySmall)
            if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 12.dp))
            if (state.failed) {
                Text(stringResource(Res.string.places_search_failed), Modifier.padding(top = 12.dp))
                TextButton(onClick = { viewModel.search(state.query, near, immediate = true) }) {
                    Text(stringResource(Res.string.places_retry))
                }
            } else if (state.searched && !state.loading && state.results.isEmpty()) {
                Text(stringResource(Res.string.places_search_empty), Modifier.padding(top = 12.dp))
            }
            LazyColumn(Modifier.heightIn(max = 320.dp)) {
                items(state.results, key = { it.id }) { place ->
                    val typeLabel = when (place.type) {
                        "city" -> Res.string.place_type_city
                        "town" -> Res.string.place_type_town
                        "village", "hamlet", "settlement" -> Res.string.place_type_village
                        "suburb" -> Res.string.place_type_area
                        "street" -> Res.string.place_type_street
                        "postcode" -> Res.string.place_type_postcode
                        "station" -> Res.string.place_type_station
                        else -> Res.string.place_type_area
                    }
                    ListItem(
                        headlineContent = { Text(place.name) },
                        supportingContent = { Text(listOf(place.subtitle, stringResource(typeLabel))
                            .filter(String::isNotBlank).joinToString(" · ")) },
                        modifier = Modifier.clickable { onSelect(place); viewModel.reset() },
                    )
                }
            }
            Text(stringResource(Res.string.places_attribution),
                style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 16.dp))
        }
        LaunchedEffect(Unit) { focus.requestFocus() }
    }
}
