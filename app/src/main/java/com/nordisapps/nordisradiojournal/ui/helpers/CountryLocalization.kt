package com.nordisapps.nordisradiojournal.ui.helpers

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nordisapps.nordisradiojournal.R

object CountryLocalization {
    @Composable
    fun displayName(raw: String): String = when (raw) {
        "Romania" -> stringResource(R.string.country_romania)
        "Ukraine" -> stringResource(R.string.country_ukraine)
        else -> raw
    }
}

object CityLocalization {
    @Composable
    fun displayName(raw: String): String = when (raw) {
        "Constanta" -> stringResource(R.string.city_constanta)
        "Brasov" -> stringResource(R.string.city_brasov)
        "Bucharest" -> stringResource(R.string.city_bucharest)
        "Odessa" -> stringResource(R.string.city_odessa)
        "Kiev" -> stringResource(R.string.city_kiev)
        "Nikolaev" -> stringResource(R.string.city_nikolaev)
        else -> raw
    }
}