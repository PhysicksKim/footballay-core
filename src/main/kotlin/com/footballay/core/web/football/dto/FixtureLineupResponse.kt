package com.footballay.core.web.football.dto

/**
 * 경기 라인업 정보 응답 DTO
 *
 * 라인업이 아직 저장되지 않았다면 빈 라인업이 제공됩니다.
 *
 * @param fixtureUid Fixture UID
 * @param lineup 홈/원정 라인업 정보
 */
data class FixtureLineupResponse(
    val fixtureUid: String,
    val lineup: Lineup,
) {
    data class Lineup(
        val home: StartLineup?,
        val away: StartLineup?,
    )

    data class StartLineup(
        val teamUid: String,
        val teamName: String,
        val teamKoreanName: String?,
        val formation: String?,
        val players: List<LineupPlayer>,
        val substitutes: List<LineupPlayer>,
        val playerColor: UniformColorDto?,
    )

    data class UniformColorDto(
        val primary: String?,
        val number: String?,
        val border: String?,
    )

    data class LineupPlayer(
        val matchPlayerUid: String,
        val playerUid: String?,
        val name: String,
        val koreanName: String?,
        val number: Int?,
        val photo: String?,
        val position: String?,
        val grid: String?,
        val substitute: Boolean,
    )
}
