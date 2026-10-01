package com.nordisapps.nordisradiojournal.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.nordisapps.nordisradiojournal.R
import com.nordisapps.nordisradiojournal.data.Station
import com.nordisapps.nordisradiojournal.ui.theme.LocalImageLoader

@Composable
fun RecentlyPlayedSection(
    recentlyPlayed: List<Station>,
    onStationClick: (Station) -> Unit
) {
    if (recentlyPlayed.isEmpty()) return

    val imageLoader = LocalImageLoader.current

    Text(
        text = stringResource(R.string.recently_played),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            count = recentlyPlayed.size,
            key = { index -> recentlyPlayed[index].id ?: index }
        ) { index ->
            val station = recentlyPlayed[index]
            Card(
                modifier = Modifier.height(140.dp),
                onClick = { onStationClick(station) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    SubcomposeAsyncImage(
                        modifier = Modifier
                            .size(64.dp)
                            .weight(1f, fill = false),
                        contentScale = ContentScale.Fit,
                        model = station.icon ?: "",
                        imageLoader = imageLoader,
                        contentDescription = station.name
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = station.name ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}