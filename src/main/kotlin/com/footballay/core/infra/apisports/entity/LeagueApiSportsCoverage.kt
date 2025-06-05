package com.footballay.core.infra.apisports.entity

import jakarta.persistence.Embeddable

@Embeddable
data class LeagueApiSportsCoverage (
    var fixturesEvents: Boolean? = null,
    var fixturesLineups: Boolean? = null,
    var fixturesStatistics: Boolean? = null,
    var fixturesPlayers: Boolean? = null,
    var standings: Boolean? = null,
    var players: Boolean? = null,
    var topScorers: Boolean? = null,
    var topAssists: Boolean? = null,
    var topCards: Boolean? = null,
    var injuries: Boolean? = null,
    var predictions: Boolean? = null,
    var odds: Boolean? = null,
)