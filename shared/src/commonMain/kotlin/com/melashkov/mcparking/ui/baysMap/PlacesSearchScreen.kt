package com.melashkov.mcparking.ui.baysMap

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
internal fun PlacesSearchScreen(
    near: GeoCoordinate?,
    onSelect: (Place) -> Unit,
    onDismiss: () -> Unit,
) {
    val viewModel = koinViewModel<PlacesSearchViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Closing or selecting removes the screen; reset the search in one place.
    DisposableEffect(viewModel) { onDispose { viewModel.reset() } }

    PlacesSearchScreenContent(
        state = state,
        onQueryChange = { viewModel.search(it, near) },
        onSearch = { viewModel.search(state.query, near, immediate = true) },
        onSelect = onSelect,
        onDismiss = onDismiss,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlacesSearchScreenContent(
    state: PlacesSearchState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelect: (Place) -> Unit,
    onDismiss: () -> Unit,
) {
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var showCredits by remember { mutableStateOf(false) }
    val dismiss = { keyboard?.hide(); onDismiss() }

    Dialog(
        onDismissRequest = dismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize().imePadding(),
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                Column(Modifier.windowInsetsPadding(TopAppBarDefaults.windowInsets)) {
                    TextField(
                        value = state.query,
                        onValueChange = onQueryChange,
                        placeholder = { Text(stringResource(Res.string.places_search_hint)) },
                        leadingIcon = {
                            IconButton(onClick = dismiss) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.action_back))
                            }
                        },
                        trailingIcon = {
                            if (state.query.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange(""); focus.requestFocus(); keyboard?.show() }) {
                                    Icon(Icons.Default.Close, stringResource(Res.string.places_clear_search))
                                }
                            }
                        },
                        singleLine = true,
                        shape = CircleShape,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch(); keyboard?.hide() }),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            .fillMaxWidth().focusRequester(focus),
                    )
                    // Reserve the progress bar's space so suggestions stay in place.
                    Box(Modifier.fillMaxWidth().height(4.dp)) {
                        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                }
            },
        ) { padding ->
            Column(Modifier.padding(padding).fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    if (state.failed) {
                        item(key = "error") {
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text(stringResource(Res.string.places_search_failed))
                                TextButton(onClick = onSearch) { Text(stringResource(Res.string.places_retry)) }
                            }
                        }
                    } else if (state.searched && !state.loading && state.results.isEmpty()) {
                        item(key = "empty") {
                            Text(stringResource(Res.string.places_search_empty), Modifier.padding(16.dp))
                        }
                    }
                    items(state.results, key = { it.id }) { place ->
                        PlaceSearchResult(place) { selected ->
                            keyboard?.hide()
                            onSelect(selected)
                        }
                    }
                }
                TextButton(
                    onClick = { showCredits = true },
                    modifier = Modifier.align(Alignment.Start).padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
                ) {
                    Text(stringResource(Res.string.places_credits_compact), style = MaterialTheme.typography.labelSmall)
                }
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
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        headlineContent = { Text(place.name) },
        supportingContent = {
            Text(listOf(place.subtitle, stringResource(typeLabel))
                .filter(String::isNotBlank).joinToString(" · "))
        },
        modifier = Modifier.clickable { onSelect(place) },
    )
}
