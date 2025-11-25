package com.footballay.core.web.football.dto

/**
 * 경기 기본 정보 응답 DTO
 *
 * @param fixtureUid Fixture UID
 * @param referee 심판 이름
 * @param date 경기 일시 (ISO 8601 format)
 * @param league 리그 정보
 * @param home 홈팀 정보
 * @param away 원정팀 정보
 */
data class FixtureInfoResponse(
    val fixtureUid: String,
    val referee: String?,
    val date: String,
    val league: LeagueInfo,
    val home: TeamInfo,
    val away: TeamInfo,
) {
    data class LeagueInfo(
        val leagueUid: String,
        val name: String,
        val koreanName: String?,
        val logo: String?,
    )

    data class TeamInfo(
        val teamUid: String,
        val name: String,
        val koreanName: String?,
        val logo: String?,
        val playerColor: UniformColorDto?,
    )

    data class UniformColorDto(
        val primary: String?,
        val number: String?,
        val border: String?,
    )
}
