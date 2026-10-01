package com.nordisapps.nordisradiojournal.data.model

enum class AnnouncementType {
    FEATURE,
    FIX,
    NEWS,
    EVENT,
    CHRISTMAS;

    companion object {
        fun fromString(value: String): AnnouncementType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: NEWS
    }
}