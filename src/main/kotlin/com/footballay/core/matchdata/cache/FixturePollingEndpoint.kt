package com.footballay.core.matchdata.cache

enum class FixturePollingEndpoint(
    val keySegment: String,
) {
    STATUS("status"),
    LINEUP("lineup"),
    EVENTS("events"),
    STATISTICS("statistics"),
}
