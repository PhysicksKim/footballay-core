package com.footballay.core.infra.apisports.dto

data class LeagueApiSportsCreateDto(
    val apiId: Long,
    val name: String,
    val type: String?,
    val logo: String?,
    val countryName: String?,
    val countryCode: String?,
    val countryFlag: String?,
    val currentSeason: Int?,
    val seasons: List<LeagueApiSportsSeasonCreateDto>
)

data class LeagueApiSportsSeasonCreateDto(
    val seasonYear: Int?,
    val seasonStart: String?,
    val seasonEnd: String?,
    val coverage: LeagueApiSportsCoverageCreateDto?
)

data class LeagueApiSportsCoverageCreateDto(
    val fixturesEvents: Boolean?,
    val fixturesLineups: Boolean?,
    val fixturesStatistics: Boolean?,
    val fixturesPlayers: Boolean?,
    val standings: Boolean?,
    val players: Boolean?,
    val topScorers: Boolean?,
    val topAssists: Boolean?,
    val topCards: Boolean?,
    val injuries: Boolean?,
    val predictions: Boolean?,
    val odds: Boolean?
)
