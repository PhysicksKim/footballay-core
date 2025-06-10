package com.footballay.core.domain.live.fetcher

enum class Side {
    HOME,
    AWAY;

    companion object {
        fun fromString(value: String): Side {
            return when (value.lowercase()) {
                "home" -> HOME
                "away" -> AWAY
                else -> throw IllegalArgumentException("Unknown side: $value")
            }
        }
    }
}