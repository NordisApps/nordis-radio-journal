package com.nordisapps.nordisradiojournal.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nordisapps.nordisradiojournal.data.model.Announcement
import com.nordisapps.nordisradiojournal.R
import com.nordisapps.nordisradiojournal.data.Station
import com.nordisapps.nordisradiojournal.data.model.RadioFact
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun HomeTab(
    isLoading: Boolean,
    announcements: List<Announcement>,
    isTranslating: Boolean,
    facts: List<RadioFact>,
    currentLanguage: String,
    recentlyPlayed: List<Station>,
    onFactsLoad: () -> Unit,
    onAnnouncementClick: (Announcement) -> Unit,
    onStationClick: (Station) -> Unit
) {
    var showLoadingIndicator by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(200.milliseconds)
            showLoadingIndicator = true
        } else {
            showLoadingIndicator = false
        }
    }

    Column(Modifier.padding(vertical = 16.dp)) {
        Text(
            text = stringResource(R.string.current_events),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        )
        when {
            isLoading && showLoadingIndicator -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(140.dp)
                )
            }

            announcements.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.EventBusy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_active_events),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(announcements) { announcement ->
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .fillParentMaxWidth(0.9f)
                        ) {
                            AnnouncementCard(
                                emoji = announcement.emoji,
                                title = announcement.title,
                                description = announcement.description,
                                imageUrl = announcement.imageUrl,
                                actionText = announcement.actionText,
                                isTranslating = isTranslating,
                                onActionClick = { onAnnouncementClick(announcement) }
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = facts.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.facts),
                    style = MaterialTheme.typography.titleMedium
                )
                FactsCarousel(
                    cards = facts,
                    currentLanguage = currentLanguage
                )
            }
        }

        LaunchedEffect(Unit) {
            onFactsLoad()
        }


        AnimatedVisibility(
            visible = recentlyPlayed.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(24.dp))
                RecentlyPlayedSection(
                    recentlyPlayed = recentlyPlayed,
                    onStationClick = onStationClick
                )
            }
        }
    }
}