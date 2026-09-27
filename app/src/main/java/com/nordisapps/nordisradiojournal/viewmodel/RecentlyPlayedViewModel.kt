package com.nordisapps.nordisradiojournal.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nordisapps.nordisradiojournal.data.RECENTLY_PLAYED_KEY
import com.nordisapps.nordisradiojournal.data.Station
import com.nordisapps.nordisradiojournal.data.dataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecentlyPlayedViewModel(
    application: Application,
    private val shared: SharedStateHolder
) : AndroidViewModel(application) {

    private val context get() = getApplication<Application>().applicationContext

    val recentlyPlayedStations: StateFlow<List<Station>> = shared.uiState
        .map { state ->
            state.recentlyPlayedIds.mapNotNull { id ->
                state.stations.find { it.id == id }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addStationToHistory(station: Station) {
        val stationId = station.id ?: return
        viewModelScope.launch {
            val currentIds = shared.uiState.value.recentlyPlayedIds.toMutableList()
            currentIds.removeAll { it == stationId }
            currentIds.add(0, stationId)
            val updatedIds = currentIds.take(3)

            shared.update { it.copy(recentlyPlayedIds = updatedIds) }
            saveRecentlyPlayed(updatedIds)
        }
    }

    private fun saveRecentlyPlayed(historyIds: List<String>) {
        viewModelScope.launch {
            val historyString = historyIds.joinToString(",")
            context.dataStore.edit { preferences ->
                preferences[RECENTLY_PLAYED_KEY] = historyString
            }
        }
    }

    fun loadRecentlyPlayed() {
        viewModelScope.launch {
            val preferences = context.dataStore.data.first()
            val historyString = preferences[RECENTLY_PLAYED_KEY] ?: ""
            val historyIds = if (historyString.isEmpty()) emptyList() else historyString.split(",")
            shared.update { it.copy(recentlyPlayedIds = historyIds) }
        }
    }
}