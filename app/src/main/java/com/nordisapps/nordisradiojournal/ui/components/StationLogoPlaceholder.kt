package com.nordisapps.nordisradiojournal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

@Composable
fun StationLogoPlaceholder(
    name: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    letterScale: Float = 0.45f
) {
    val letter = name
        ?.trim()
        ?.firstOrNull { it.isLetterOrDigit() }
        ?.uppercase()
        ?: "?"

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = (size.value * letterScale).sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}