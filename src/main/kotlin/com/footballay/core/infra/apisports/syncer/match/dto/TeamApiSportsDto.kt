package com.footballay.core.infra.apisports.syncer.match.dto

data class TeamApiSportsDto(
    val apiId: Long,
    val name: String?,
    val code: String?,
    val logo: String?
)