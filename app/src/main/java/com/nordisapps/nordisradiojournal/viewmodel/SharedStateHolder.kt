package com.nordisapps.nordisradiojournal.viewmodel

import com.nordisapps.nordisradiojournal.data.Station
import com.nordisapps.nordisradiojournal.data.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SharedStateHolder {
    private val _uiState = MutableStateFlow(UiState(isLoading = true))
    val uiState: StateFlow<UiState> = _uiState
    val stations = MutableStateFlow<List<Station>>(emptyList())

    fun update(block: (UiState) -> UiState) {
        _uiState.update(block)
    }
}