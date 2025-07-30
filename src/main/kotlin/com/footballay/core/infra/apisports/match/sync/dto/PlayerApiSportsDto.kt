package com.footballay.core.infra.apisports.match.sync.dto

data class PlayerApiSportsDto(
    val apiId: Long,
    val name: String?,
    val position: String?,
    val photo: String?
)