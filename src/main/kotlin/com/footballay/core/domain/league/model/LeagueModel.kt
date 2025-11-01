package com.footballay.core.domain.league.model

sealed interface LeagueExtra

data class LeagueModel(
    val id: Long,
    val uid: String?,
    val name: String,
    val season: Int?,
    val available: Boolean,
    val extra: LeagueExtra? = null,
)
