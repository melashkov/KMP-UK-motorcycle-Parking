package com.melashkov.mcparking.ui.baysMap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.Place
import com.melashkov.mcparking.domain.usecases.SearchPlacesResult
import com.melashkov.mcparking.domain.usecases.SearchPlacesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Duration.Companion.milliseconds

data class PlacesSearchState(
    val query: String = "",
    val results: List<Place> = emptyList(),
    val loading: Boolean = false,
    val failed: Boolean = false,
    val searched: Boolean = false,
)

@KoinViewModel
class PlacesSearchViewModel(private val searchPlaces: SearchPlacesUseCase) : ViewModel() {
    private val searchDebounce = 700.milliseconds

    private val state = MutableStateFlow(PlacesSearchState())
    val uiState = state.asStateFlow()
    private var job: Job? = null

    fun search(query: String, near: GeoCoordinate?, immediate: Boolean = false) {
        job?.cancel()
        state.value = PlacesSearchState(query = query)
        job = viewModelScope.launch {
            if (!immediate) delay(searchDebounce)
            state.value = state.value.copy(loading = true)
            val result = searchPlaces(query, near)
            ensureActive()
            state.value = when (result) {
                SearchPlacesResult.QueryTooShort -> state.value.copy(loading = false)
                is SearchPlacesResult.Success -> state.value.copy(results = result.places, loading = false, searched = true)
                is SearchPlacesResult.Failure -> state.value.copy(failed = true, loading = false, searched = true)
            }
        }
    }

    fun reset() {
        job?.cancel()
        state.value = PlacesSearchState()
    }
}
