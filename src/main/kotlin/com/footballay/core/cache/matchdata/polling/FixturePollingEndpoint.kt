package com.footballay.core.cache.matchdata.polling

enum class FixturePollingEndpoint(
    val keySegment: String,
) {
    STATUS("status"),
    LINEUP("lineup"),
    EVENTS("events"),
    STATISTICS("statistics"),
}
