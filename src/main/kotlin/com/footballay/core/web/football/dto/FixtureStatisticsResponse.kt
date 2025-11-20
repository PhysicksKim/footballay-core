package com.footballay.core.web.football.dto

/**
 * 경기 통계 응답 DTO
 *
 * @param fixture 경기 기본 정보
 * @param home 홈팀 통계 (팀 + 선수별 통계)
 * @param away 원정팀 통계 (팀 + 선수별 통계)
 */
data class FixtureStatisticsResponse(
    val fixture: FixtureBasic,
    val home: TeamWithStatistics,
    val away: TeamWithStatistics,
) {
    data class FixtureBasic(
        val uid: String,
        val elapsed: Int?,
        val status: String,
    )

    data class TeamInfo(
        val teamUid: String,
        val name: String,
        val koreanName: String?,
        val logo: String?,
    )

    data class XG(
        val elapsed: Int,
        val xg: String,
    )

    data class TeamStatistics(
        val shotsOnGoal: Int,
        val shotsOffGoal: Int,
        val totalShots: Int,
        val blockedShots: Int,
        val shotsInsideBox: Int,
        val shotsOutsideBox: Int,
        val fouls: Int,
        val cornerKicks: Int,
        val offsides: Int,
        val ballPossession: Int,
        val yellowCards: Int,
        val redCards: Int,
        val goalkeeperSaves: Int,
        val totalPasses: Int,
        val passesAccurate: Int,
        val passesAccuracyPercentage: Int,
        val goalsPrevented: Int,
        val xg: List<XG>,
    )

    data class PlayerInfo(
        val matchPlayerUid: String,
        val playerUid: String?,
        val name: String,
        val koreanName: String?,
        val photo: String?,
        val position: String?,
        val number: Int?,
    )

    data class PlayerStatistics(
        val minutesPlayed: Int,
        val position: String?,
        val rating: String?,
        val captain: Boolean,
        val substitute: Boolean,
        val shotsTotal: Int,
        val shotsOn: Int,
        val goals: Int,
        val goalsConceded: Int,
        val assists: Int,
        val saves: Int,
        val passesTotal: Int,
        val passesKey: Int,
        val passesAccuracy: Int,
        val tacklesTotal: Int,
        val interceptions: Int,
        val duelsTotal: Int,
        val duelsWon: Int,
        val dribblesAttempts: Int,
        val dribblesSuccess: Int,
        val foulsCommitted: Int,
        val foulsDrawn: Int,
        val yellowCards: Int,
        val redCards: Int,
        val penaltiesScored: Int,
        val penaltiesMissed: Int,
        val penaltiesSaved: Int,
    )

    data class PlayerWithStatistics(
        val player: PlayerInfo,
        val statistics: PlayerStatistics,
    )

    data class TeamWithStatistics(
        val team: TeamInfo,
        val teamStatistics: TeamStatistics,
        val playerStatistics: List<PlayerWithStatistics>,
    )
}
