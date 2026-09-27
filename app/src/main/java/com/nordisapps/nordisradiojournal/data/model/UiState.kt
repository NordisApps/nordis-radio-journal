package com.nordisapps.nordisradiojournal.data.model

import com.nordisapps.nordisradiojournal.data.Station

sealed class AdminState {
    object Unknown : AdminState()
    object Admin : AdminState()
    object NotAdmin : AdminState()
}

data class UiState(
    val stations: List<Station> = emptyList(),
    val isLoading: Boolean = true,
    val currentStation: Station? = null,
    val isPlaying: Boolean = false,
    val currentTrackTitle: String? = null,
    val currentBitrate: Int? = null,
    val recentlyPlayedIds: List<String> = emptyList(),
    val favouriteIds: List<String> = emptyList(),
    val adminState: AdminState = AdminState.Unknown,
    val isUserLoggedIn: Boolean = false,
    val activeTimerMinutes: String? = null,
    val endTimerTime: String? = null,
    val announcements: List<Announcement> = emptyList()
)