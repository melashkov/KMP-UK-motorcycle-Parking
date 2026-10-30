package com.melashkov.mcparking.ui.baysMap

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

@Composable
internal fun PlacesSearchSheet(
    near: GeoCoordinate?,
    onSelect: (Place) -> Unit,
    onDismiss: () -> Unit,
) {
    val viewModel = koinViewModel<PlacesSearchViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Closing or selecting removes the sheet; reset the search in one place.
    DisposableEffect(viewModel) { onDispose { viewModel.reset() } }

    PlacesSearchSheetContent(
        state = state,
        onQueryChange = { viewModel.search(it, near) },
        onSearch = { viewModel.search(state.query, near, immediate = true) },
        onSelect = onSelect,
        onDismiss = onDismiss,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlacesSearchSheetContent(
    state: PlacesSearchState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelect: (Place) -> Unit,
    onDismiss: () -> Unit,
) {
    val focus = remember { FocusRequester() }
    var showCredits by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).imePadding()
                .verticalScroll(rememberScrollState()),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                label = { Text(stringResource(Res.string.places_search_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
            )
            Spacer(Modifier.height(12.dp))
            if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 12.dp))
            if (state.failed) {
                Text(stringResource(Res.string.places_search_failed), Modifier.padding(top = 12.dp))
                TextButton(onClick = onSearch) { Text(stringResource(Res.string.places_retry)) }
            } else if (state.searched && !state.loading && state.results.isEmpty()) {
                Text(stringResource(Res.string.places_search_empty), Modifier.padding(top = 12.dp))
            }
            state.results.forEach { place ->
                key(place.id) { PlaceSearchResult(place, onSelect) }
            }
            TextButton(
                onClick = { showCredits = true },
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
            ) {
                Text(stringResource(Res.string.places_credits_compact), style = MaterialTheme.typography.labelSmall)
            }
        }
        LaunchedEffect(focus) { focus.requestFocus() }
    }
    if (showCredits) PlacesDataCreditsDialog(onDismiss = { showCredits = false })
}

@Composable
private fun PlaceSearchResult(place: Place, onSelect: (Place) -> Unit) {
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
        supportingContent = {
            Text(listOf(place.subtitle, stringResource(typeLabel))
                .filter(String::isNotBlank).joinToString(" · "))
        },
        modifier = Modifier.clickable { onSelect(place) },
    )
}
