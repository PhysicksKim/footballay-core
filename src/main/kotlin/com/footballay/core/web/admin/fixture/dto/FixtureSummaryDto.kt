package com.footballay.core.web.admin.fixture.dto

data class FixtureSummaryDto(
    val uid: String,
    val kickoffAt: String,
    val homeTeam: String,
    val awayTeam: String,
    val status: String,
    val available: Boolean,
)


