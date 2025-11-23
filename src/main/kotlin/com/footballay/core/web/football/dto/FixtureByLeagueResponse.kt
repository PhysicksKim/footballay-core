package com.footballay.core.web.football.dto

/**
 * Desktop App용 리그별 경기 응답 DTO
 */
data class FixtureByLeagueResponse(
    val uid: String,
    val kickoff: String?,
    val round: String,
    val homeTeam: TeamInfo,
    val awayTeam: TeamInfo,
    val status: StatusInfo,
    val score: ScoreInfo,
    val available: Boolean,
) {
    data class TeamInfo(
        val uid: String?,
        val name: String,
        val nameKo: String?,
        val logo: String?,
    )

    data class StatusInfo(
        val longStatus: String,
        val shortStatus: String,
        val elapsed: Int?,
    )

    data class ScoreInfo(
        val home: Int?,
        val away: Int?,
    )
}
