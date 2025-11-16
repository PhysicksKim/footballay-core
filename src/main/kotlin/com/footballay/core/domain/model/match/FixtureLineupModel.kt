package com.footballay.core.domain.model.match

/**
 * 경기 라인업 도메인 모델
 *
 * Query Service → Web Layer 전달용 도메인 모델
 */
data class FixtureLineupModel(
    val fixtureUid: String,
    val lineup: Lineup,
) {
    data class Lineup(
        val home: StartLineup,
        val away: StartLineup,
    )

    data class StartLineup(
        val teamId: Long,
        val teamName: String,
        val teamKoreanName: String?,
        val formation: String?,
        val players: List<LineupPlayer>,
        val substitutes: List<LineupPlayer>,
    )

    data class LineupPlayer(
        val id: Long,
        val name: String,
        val koreanName: String?,
        val number: Int?,
        val photo: String?,
        val position: String?,
        val grid: String?,
        val substitute: Boolean,
        val tempId: String?,
    )
}
