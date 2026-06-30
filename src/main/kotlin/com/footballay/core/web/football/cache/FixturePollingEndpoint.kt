package com.footballay.core.web.football.cache

enum class FixturePollingEndpoint(
    val keySegment: String,
) {
    STATUS("status"),
    LINEUP("lineup"),
    EVENTS("events"),
    STATISTICS("statistics"),
}
