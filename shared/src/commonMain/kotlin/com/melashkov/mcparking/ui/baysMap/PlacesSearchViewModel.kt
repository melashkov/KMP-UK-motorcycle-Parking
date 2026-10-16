package com.melashkov.mcparking.ui.baysMap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melashkov.mcparking.domain.entity.GeoCoordinate
import com.melashkov.mcparking.domain.entity.Place
import com.melashkov.mcparking.domain.interfaces.DataResult
import com.melashkov.mcparking.domain.interfaces.PlacesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

data class PlacesSearchState(
    val query: String = "",
    val results: List<Place> = emptyList(),
    val loading: Boolean = false,
    val failed: Boolean = false,
    val searched: Boolean = false,
)

@KoinViewModel
class PlacesSearchViewModel(private val repository: PlacesRepository) : ViewModel() {
    private val state = MutableStateFlow(PlacesSearchState())
    val uiState = state.asStateFlow()
    private var job: Job? = null
    private var generation = 0

    fun search(query: String, near: GeoCoordinate?, immediate: Boolean = false) {
        job?.cancel()
        val request = ++generation
        val trimmed = query.trim().take(80)
        state.value = PlacesSearchState(query = query.take(80))
        if (trimmed.length < 3) return
        job = viewModelScope.launch {
            if (!immediate) delay(350)
            state.value = state.value.copy(loading = true)
            val result = repository.search(trimmed, near)
            if (request != generation) return@launch
            state.value = when (result) {
                is DataResult.Success -> state.value.copy(results = result.value, loading = false, searched = true)
                is DataResult.Failure -> state.value.copy(failed = true, loading = false, searched = true)
            }
        }
    }

    fun reset() {
        ++generation
        job?.cancel()
        state.value = PlacesSearchState()
    }
}
