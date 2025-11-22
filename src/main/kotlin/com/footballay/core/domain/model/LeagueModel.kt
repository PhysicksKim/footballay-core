package com.footballay.core.domain.model

sealed interface LeagueExtension

data class LeagueModel(
    val uid: String,
    val name: String,
    val nameKo: String?,
    val photo: String?,
    val available: Boolean,
    val extension: LeagueExtension = NoLeagueExtension,
)

object NoLeagueExtension : LeagueExtension

data class LeagueApiSportsExtension(
    val apiId: Long,
) : LeagueExtension
