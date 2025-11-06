package com.footballay.core.infra.apisports.match.sync.dto

data class MatchPlayerDto(
    val matchPlayerUid: String? = null, // null 인 경우 uid 생성을 deferred
    val apiId: Long?,
    val name: String,
    val number: Int? = null,
    val position: String? = null,
    val grid: String? = null,
    val substitute: Boolean,
    val nonLineupPlayer: Boolean = false, // lineup 에 등장하지 않았으나 다른 맥락(ex.event) 에 등장한 사람
    val teamApiId: Long?, // nullable: 팀 불명확한 경우
    val playerApiSportsInfo: PlayerApiSportsDto?, // nullable: apiId가 null인 경우
)
