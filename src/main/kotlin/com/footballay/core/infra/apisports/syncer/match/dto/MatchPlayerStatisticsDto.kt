package com.footballay.core.infra.apisports.syncer.match.dto

data class MatchPlayerStatisticsDto(
    val playerApiId: Long?,
    val name: String,
    val teamApiId: Long?
    // TODO: 통계 필드들 추가
)
