package com.nordisapps.nordisradiojournal.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.nordisapps.nordisradiojournal.data.FAVORITE_STATIONS_KEY
import com.nordisapps.nordisradiojournal.data.Station
import com.nordisapps.nordisradiojournal.data.dataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavouritesViewModel(
    application: Application,
    private val shared: SharedStateHolder
) : AndroidViewModel(application) {

    private val context get() = getApplication<Application>().applicationContext

    val favouriteStations: StateFlow<List<Station>> = combine(
        shared.uiState,
        shared.stations
    ) { state, stations ->
        state.favouriteIds.mapNotNull { id ->
            stations.find { it.id == id }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleFavourite(station: Station) {
        val stationId = station.id ?: return
        val currentIds = shared.uiState.value.favouriteIds.toMutableList()
        if (currentIds.contains(stationId)) {
            currentIds.remove(stationId)
        } else {
            currentIds.add(stationId)
        }
        shared.update { it.copy(favouriteIds = currentIds) }
        saveFavourites(currentIds)
    }

    private fun saveFavourites(favoriteIds: List<String>) {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            val idsSet = favoriteIds.toSet()
            if (user != null) {
                FirebaseDatabase.getInstance()
                    .getReference("favorites")
                    .child(user.uid)
                    .setValue(idsSet.toList())
            } else {
                context.dataStore.edit { preferences ->
                    preferences[FAVORITE_STATIONS_KEY] = idsSet
                }
            }
        }
    }

    fun loadFavourites() {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                FirebaseDatabase.getInstance()
                    .getReference("favorites")
                    .child(user.uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val favoriteIds =
                            snapshot.children.mapNotNull { it.getValue(String::class.java) }
                        shared.update { it.copy(favouriteIds = favoriteIds) }
                    }
            } else {
                val preferences = context.dataStore.data.first()
                val favoriteIds = (preferences[FAVORITE_STATIONS_KEY] ?: emptySet()).toList()
                shared.update { it.copy(favouriteIds = favoriteIds) }
            }
        }
    }

    fun mergeFavouritesOnLogin(uid: String) {
        viewModelScope.launch {
            val preferences = context.dataStore.data.first()
            val localIds = preferences[FAVORITE_STATIONS_KEY] ?: emptySet()

            if (localIds.isEmpty()) {
                loadFavourites()
                return@launch
            }

            val ref = FirebaseDatabase.getInstance()
                .getReference("favorites")
                .child(uid)

            ref.get().addOnSuccessListener { snapshot ->
                val firebaseIds = snapshot.children
                    .mapNotNull { it.getValue(String::class.java) }
                    .toSet()

                val mergedIds = (localIds + firebaseIds).toList()

                ref.setValue(mergedIds).addOnSuccessListener {
                    viewModelScope.launch {
                        context.dataStore.edit { prefs ->
                            prefs[FAVORITE_STATIONS_KEY] = emptySet()
                        }
                    }
                    shared.update { it.copy(favouriteIds = mergedIds) }
                }
            }
        }
    }
}