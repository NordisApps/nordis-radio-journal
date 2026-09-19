@file:Suppress("AssignedValueIsNeverRead")

package com.nordisapps.nordisradiojournal.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nordisapps.nordisradiojournal.R
import com.nordisapps.nordisradiojournal.data.Station
import com.nordisapps.nordisradiojournal.ui.components.RadioStationItem
import com.nordisapps.nordisradiojournal.ui.helpers.CityLocalization
import com.nordisapps.nordisradiojournal.ui.helpers.CountryLocalization
import com.nordisapps.nordisradiojournal.ui.helpers.rememberCategoryDisplayNames

data class LocationItem(val key: String, val displayName: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTab(
    searchQuery: String,
    selectedCountryKey: String?,
    selectedCityKey: String?,
    selectedCoverageKeys: Set<String>,
    availableCountries: List<String>,
    citiesByCountry: Map<String, List<String>>,
    filteredStations: List<Station>,
    favourites: List<Station>,
    onSearchQueryChange: (String) -> Unit,
    onCountrySelected: (String) -> Unit,
    onCitySelected: (String?) -> Unit,
    onCoverageSelected: (Set<String>) -> Unit,
    onFavouriteClick: (Station) -> Unit,
    onListenClick: (Station) -> Unit,
    selectedCategoryKeys: Set<String>,
    onCategorySelected: (Set<String>) -> Unit,
    hasMiniPlayer: Boolean
) {
    var isSearchFocused by rememberSaveable { mutableStateOf(false) }

    val countries = remember(availableCountries) {
        availableCountries.map { LocationItem(key = it, displayName = it) }
    }

    val keyMusic = stringResource(R.string.key_category_music)
    val displayMusic = stringResource(R.string.category_music)
    val keyNews = stringResource(R.string.key_category_news)
    val displayNews = stringResource(R.string.category_news)
    val keyTalk = stringResource(R.string.key_category_talk)
    val displayTalk = stringResource(R.string.category_talk)
    val keyChurch = stringResource(R.string.key_category_church)
    val displayChurch = stringResource(R.string.category_church)
    val keyChildren = stringResource(R.string.key_category_children)
    val displayChildren = stringResource(R.string.category_children)
    val keySports = stringResource(R.string.key_category_sports)
    val displaySports = stringResource(R.string.category_sports)
    val keyCultural = stringResource(R.string.key_category_cultural)
    val displayCultural = stringResource(R.string.category_cultural)
    val keyRegional = stringResource(R.string.key_category_regional)
    val displayRegional = stringResource(R.string.category_regional)

    val categoryOptions = remember(
        keyMusic, displayMusic, keyNews, displayNews, keyTalk, displayTalk,
        keyChurch, displayChurch, keyChildren, displayChildren, keySports, displaySports,
        keyCultural, displayCultural, keyRegional, displayRegional
    ) {
        listOf(
            LocationItem(keyMusic, displayMusic),
            LocationItem(keyNews, displayNews),
            LocationItem(keyTalk, displayTalk),
            LocationItem(keyChurch, displayChurch),
            LocationItem(keyChildren, displayChildren),
            LocationItem(keySports, displaySports),
            LocationItem(keyCultural, displayCultural),
            LocationItem(keyRegional, displayRegional)
        )
    }

    var showCountrySheet by remember { mutableStateOf(false) }
    var showCitySheet by remember { mutableStateOf(false) }
    var showCoverageSheet by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    BackHandler(enabled = isSearchFocused || searchQuery.isNotEmpty()) {
        focusManager.clearFocus(force = true)
        onSearchQueryChange("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp, max = 56.dp)
                    .onFocusChanged { focusState ->
                        isSearchFocused = focusState.isFocused
                    },
                shape = RoundedCornerShape(50.dp),
                placeholder = {
                    Box(
                        modifier = Modifier.fillMaxHeight(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            stringResource(R.string.search_placeholder),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = selectedCountryKey != null,
                    onClick = { showCountrySheet = true },
                    label = {
                        Text(
                            text = selectedCountryKey?.let { CountryLocalization.displayName(it) }
                                ?: stringResource(R.string.select_country)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                )

                if (showCountrySheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showCountrySheet = false }
                    ) {
                        Text(
                            text = stringResource(R.string.select_country_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            items(countries) { countryItem ->
                                Text(
                                    text = CountryLocalization.displayName(countryItem.key),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onCountrySelected(countryItem.key)
                                            onCitySelected(null)
                                            showCountrySheet = false
                                        }
                                        .padding(horizontal = 24.dp, vertical = 16.dp)
                                )
                            }
                        }
                    }
                }

                if (selectedCountryKey != null) {
                    val cities = remember(citiesByCountry, selectedCountryKey) {
                        (citiesByCountry[selectedCountryKey] ?: emptyList()).map { LocationItem(it, it) }
                    }

                    FilterChip(
                        selected = selectedCityKey != null,
                        onClick = { showCitySheet = true },
                        label = {
                            Text(
                                text = selectedCityKey?.let { CityLocalization.displayName(it) }
                                    ?: stringResource(R.string.select_city)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationCity,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    )

                    if (showCitySheet) {
                        val citySheetState = rememberModalBottomSheetState(
                            skipPartiallyExpanded = true
                        )

                        ModalBottomSheet(
                            onDismissRequest = { showCitySheet = false },
                            sheetState = citySheetState
                        ) {
                            Text(
                                text = stringResource(R.string.select_city_title),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                items(cities) { cityItem ->
                                    Text(
                                        text = CityLocalization.displayName(cityItem.key),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onCitySelected(cityItem.key)
                                                showCitySheet = false
                                            }
                                            .padding(horizontal = 24.dp, vertical = 16.dp)
                                    )
                                }
                            }
                        }
                    }

                    val coverageOptions = remember(citiesByCountry, selectedCountryKey) {
                        (citiesByCountry[selectedCountryKey] ?: emptyList()).map { LocationItem(it, it) }
                    }

                    FilterChip(
                        selected = selectedCoverageKeys.isNotEmpty(),
                        onClick = { showCoverageSheet = true },
                        label = {
                            Text(
                                text = if (selectedCoverageKeys.isEmpty())
                                    stringResource(R.string.select_coverage)
                                else
                                    "${selectedCoverageKeys.size} ${stringResource(R.string.selected)}"
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.SettingsInputAntenna,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    )

                    if (showCoverageSheet) {
                        var draftCoverage by remember(selectedCoverageKeys) {
                            mutableStateOf(selectedCoverageKeys)
                        }

                        val coverageSheetState = rememberModalBottomSheetState(
                            skipPartiallyExpanded = true
                        )

                        ModalBottomSheet(
                            onDismissRequest = { showCoverageSheet = false },
                            sheetState = coverageSheetState
                        ) {
                            Text(
                                text = stringResource(R.string.coverage_title),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(coverageOptions) { coverageItem ->
                                    val isChecked = coverageItem.key in draftCoverage

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                draftCoverage = if (isChecked) {
                                                    draftCoverage - coverageItem.key
                                                } else {
                                                    draftCoverage + coverageItem.key
                                                }
                                            }
                                            .padding(horizontal = 24.dp, vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = null
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(text = CityLocalization.displayName(coverageItem.key))
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(onClick = { draftCoverage = emptySet() }) {
                                    Text(stringResource(R.string.clear))
                                }
                                Button(
                                    onClick = {
                                        onCoverageSelected(draftCoverage)
                                        showCoverageSheet = false
                                    }
                                ) {
                                    Text(stringResource(R.string.apply))
                                }
                            }
                        }
                    }
                }

                FilterChip(
                    selected = selectedCategoryKeys.isNotEmpty(),
                    onClick = { showCategorySheet = true },
                    label = {
                        Text(
                            text = if (selectedCategoryKeys.isEmpty())
                                stringResource(R.string.select_category)
                            else
                                "${selectedCategoryKeys.size} ${stringResource(R.string.selected)}"
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                )

                if (showCategorySheet) {
                    var draftCategory by remember(selectedCategoryKeys) {
                        mutableStateOf(selectedCategoryKeys)
                    }

                    val categorySheetState = rememberModalBottomSheetState(
                        skipPartiallyExpanded = true
                    )

                    ModalBottomSheet(
                        onDismissRequest = { showCategorySheet = false },
                        sheetState = categorySheetState
                    ) {
                        Text(
                            text = stringResource(R.string.categories_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(categoryOptions) { categoryItem ->
                                val isChecked = categoryItem.key in draftCategory

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            draftCategory = if (isChecked) {
                                                draftCategory - categoryItem.key
                                            } else {
                                                draftCategory + categoryItem.key
                                            }
                                        }
                                        .padding(horizontal = 24.dp, vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(checked = isChecked, onCheckedChange = null)
                                    Spacer(Modifier.width(12.dp))
                                    Text(text = categoryItem.displayName)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { draftCategory = emptySet() }) {
                                Text(stringResource(R.string.clear))
                            }
                            Button(
                                onClick = {
                                    onCategorySelected(draftCategory)
                                    showCategorySheet = false
                                }
                            ) {
                                Text(stringResource(R.string.apply))
                            }
                        }
                    }
                }
            }
        }

        val isFilterActive =
            searchQuery.isNotEmpty() ||
                    selectedCountryKey != null ||
                    selectedCityKey != null ||
                    selectedCoverageKeys.isNotEmpty() ||
                    selectedCategoryKeys.isNotEmpty()
        val showPrompt = !isFilterActive

        if (showPrompt) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.search_or_filter_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            if (filteredStations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_stations_found))
                }
            } else {
                val categoryDisplayNames = rememberCategoryDisplayNames()

                LazyColumn(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(bottom = if (hasMiniPlayer) 88.dp else 0.dp)
                ) {
                    items(
                        filteredStations,
                        key = { it.id ?: it.name ?: "" }) { station ->
                        RadioStationItem(
                            icon = station.icon ?: "",
                            name = station.name ?: "",
                            freq = station.freq ?: "",
                            city = station.stationCity ?: "",
                            category = station.category,
                            categoryDisplayNames = categoryDisplayNames,
                            coverage = station.coverage,
                            mainCity = station.mainCity,
                            location = station.location ?: "",
                            ps = station.ps ?: "",
                            rt = station.rt ?: "",
                            hasIssues = station.hasIssues ?: false,
                            stream = station.stream,
                            isFavourite = favourites.any { it.id == station.id },
                            onFavouriteClick = { onFavouriteClick(station) },
                            onListenClick = { onListenClick(station) }
                        )
                    }
                }
            }
        }
    }
}