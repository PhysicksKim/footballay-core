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
        val teamId: Long = 0, // Deprecated: PK 노출 방지
        val teamName: String,
        val teamKoreanName: String?,
        val formation: String?,
        val players: List<LineupPlayer>,
        val substitutes: List<LineupPlayer>,
        val teamUid: String,
    )

    data class LineupPlayer(
        val id: Long = 0, // Deprecated: PK 노출 방지
        val name: String,
        val koreanName: String?,
        val number: Int?,
        val photo: String?,
        val position: String?,
        val grid: String?,
        val substitute: Boolean,
        val matchPlayerUid: String,
        val playerUid: String?,
    )
}
