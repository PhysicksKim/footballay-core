package com.footballay.core.infra.apisports.syncer.match.dto

data class PlayerApiSportsDto(
    val apiId: Long,
    val name: String?,
    val position: String?,
    val photo: String?
)