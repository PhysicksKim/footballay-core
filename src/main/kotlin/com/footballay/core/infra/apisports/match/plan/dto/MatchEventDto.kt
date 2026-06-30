package com.footballay.core.infra.apisports.match.plan.dto

/**
 * eventType 이 subst 인 경우 player 가 sub-in 이고 assist 가 sub-out 입니다.
 */
data class MatchEventDto(
    val sequence: Int,
    val elapsedTime: Int,
    val extraTime: Int?,
    val eventType: String,
    val detail: String?,
    val comments: String?,
    val teamApiId: Long?, // nullable: 팀 불명확한 이벤트
    val playerMpKey: String?, // nullable: 선수 무관한 이벤트
    val assistMpKey: String?, // nullable: 어시스트 없는 경우
)
