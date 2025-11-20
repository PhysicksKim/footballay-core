package com.footballay.core.domain.model

sealed interface LeagueExtra

data class LeagueModel(
    val photo: String?,
    val uid: String,
    val name: String,
    val nameKo: String?,
    val season: Int?,
    val available: Boolean,
    val extra: LeagueExtra? = null,
)

data class LeagueApiSportsExtra(
    val apiId: Long,
) : LeagueExtra
