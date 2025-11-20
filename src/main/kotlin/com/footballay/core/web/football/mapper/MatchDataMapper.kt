package com.footballay.core.web.football.mapper

import com.footballay.core.domain.model.match.*
import com.footballay.core.web.football.dto.*
import com.footballay.core.logger
import org.springframework.stereotype.Component

/**
 * Match Data Domain Model → Response DTO 변환 Mapper
 *
 * **책임:**
 * - Domain Model을 Web Response DTO로 변환
 * - null 안전성 보장
 * - 클라이언트 API 계약에 맞는 형식으로 변환
 */
@Component
class MatchDataMapper {
    private val log = logger()

    /**
     * Fixture 기본 정보 변환 (Domain Model → Response DTO)
     */
    fun toFixtureInfoResponse(model: FixtureInfoModel): FixtureInfoResponse =
        FixtureInfoResponse(
            fixtureUid = model.fixtureUid,
            referee = model.referee,
            date = model.date,
            league =
                FixtureInfoResponse.LeagueInfo(
                    leagueUid = model.league.leagueUid,
                    name = model.league.name,
                    koreanName = model.league.koreanName,
                    logo = model.league.logo,
                ),
            home =
                FixtureInfoResponse.TeamInfo(
                    teamUid = model.home.teamUid,
                    name = model.home.name,
                    koreanName = model.home.koreanName,
                    logo = model.home.logo,
                ),
            away =
                FixtureInfoResponse.TeamInfo(
                    teamUid = model.away.teamUid,
                    name = model.away.name,
                    koreanName = model.away.koreanName,
                    logo = model.away.logo,
                ),
        )

    /**
     * Fixture 라이브 상태 변환 (Domain Model → Response DTO)
     */
    fun toFixtureLiveStatusResponse(model: FixtureLiveStatusModel): FixtureLiveStatusResponse =
        FixtureLiveStatusResponse(
            fixtureUid = model.fixtureUid,
            liveStatus =
                FixtureLiveStatusResponse.LiveStatus(
                    elapsed = model.liveStatus.elapsed,
                    shortStatus = model.liveStatus.shortStatus,
                    longStatus = model.liveStatus.longStatus,
                    score =
                        FixtureLiveStatusResponse.Score(
                            home = model.liveStatus.score.home,
                            away = model.liveStatus.score.away,
                        ),
                ),
        )

    /**
     * 경기 이벤트 목록 변환 (Domain Model → Response DTO)
     */
    fun toFixtureEventsResponse(model: FixtureEventsModel): FixtureEventsResponse =
        FixtureEventsResponse(
            fixtureUid = model.fixtureUid,
            events = model.events.map { toEventInfo(it) },
        )

    private fun toEventInfo(event: FixtureEventsModel.EventInfo): FixtureEventsResponse.EventInfo =
        FixtureEventsResponse.EventInfo(
            sequence = event.sequence,
            elapsed = event.elapsed,
            extraTime = event.extraTime,
            team =
                FixtureEventsResponse.TeamInfo(
                    teamUid = event.team.teamUid,
                    name = event.team.name,
                    koreanName = event.team.koreanName,
                ),
            player = event.player?.let { toPlayerInfo(it) },
            assist = event.assist?.let { toPlayerInfo(it) },
            type = event.type,
            detail = event.detail,
            comments = event.comments,
        )

    private fun toPlayerInfo(player: FixtureEventsModel.PlayerInfo): FixtureEventsResponse.PlayerInfo =
        FixtureEventsResponse.PlayerInfo(
            matchPlayerUid = player.matchPlayerUid ?: "",
            playerUid = player.playerUid,
            name = player.name ?: "",
            koreanName = player.koreanName,
            number = player.number,
        )

    /**
     * 경기 라인업 변환 (Domain Model → Response DTO)
     */
    fun toFixtureLineupResponse(model: FixtureLineupModel): FixtureLineupResponse =
        FixtureLineupResponse(
            fixtureUid = model.fixtureUid,
            lineup =
                FixtureLineupResponse.Lineup(
                    home = toStartLineup(model.lineup.home),
                    away = toStartLineup(model.lineup.away),
                ),
        )

    private fun toStartLineup(lineup: FixtureLineupModel.StartLineup): FixtureLineupResponse.StartLineup =
        FixtureLineupResponse.StartLineup(
            teamUid = lineup.teamUid,
            teamName = lineup.teamName,
            teamKoreanName = lineup.teamKoreanName,
            formation = lineup.formation,
            players = lineup.players.map { toLineupPlayer(it) },
            substitutes = lineup.substitutes.map { toLineupPlayer(it) },
        )

    private fun toLineupPlayer(player: FixtureLineupModel.LineupPlayer): FixtureLineupResponse.LineupPlayer =
        FixtureLineupResponse.LineupPlayer(
            matchPlayerUid = player.matchPlayerUid,
            playerUid = player.playerUid,
            name = player.name,
            koreanName = player.koreanName,
            number = player.number,
            photo = player.photo,
            position = player.position,
            grid = player.grid,
            substitute = player.substitute,
        )

    /**
     * 경기 통계 변환 (Domain Model → Response DTO)
     */
    fun toFixtureStatisticsResponse(model: FixtureStatisticsModel): FixtureStatisticsResponse =
        FixtureStatisticsResponse(
            fixture =
                FixtureStatisticsResponse.FixtureBasic(
                    uid = model.fixture.uid,
                    elapsed = model.fixture.elapsed,
                    status = model.fixture.status,
                ),
            home = toTeamWithStatistics(model.home),
            away = toTeamWithStatistics(model.away),
        )

    private fun toTeamWithStatistics(team: FixtureStatisticsModel.TeamWithStatistics): FixtureStatisticsResponse.TeamWithStatistics =
        FixtureStatisticsResponse.TeamWithStatistics(
            team =
                FixtureStatisticsResponse.TeamInfo(
                    teamUid = team.team.teamUid,
                    name = team.team.name,
                    koreanName = team.team.koreanName,
                    logo = team.team.logo,
                ),
            teamStatistics = toTeamStatistics(team.teamStatistics),
            playerStatistics = team.playerStatistics.map { toPlayerWithStatistics(it) },
        )

    private fun toTeamStatistics(stats: FixtureStatisticsModel.TeamStatistics): FixtureStatisticsResponse.TeamStatistics =
        FixtureStatisticsResponse.TeamStatistics(
            shotsOnGoal = stats.shotsOnGoal,
            shotsOffGoal = stats.shotsOffGoal,
            totalShots = stats.totalShots,
            blockedShots = stats.blockedShots,
            shotsInsideBox = stats.shotsInsideBox,
            shotsOutsideBox = stats.shotsOutsideBox,
            fouls = stats.fouls,
            cornerKicks = stats.cornerKicks,
            offsides = stats.offsides,
            ballPossession = stats.ballPossession,
            yellowCards = stats.yellowCards,
            redCards = stats.redCards,
            goalkeeperSaves = stats.goalkeeperSaves,
            totalPasses = stats.totalPasses,
            passesAccurate = stats.passesAccurate,
            passesAccuracyPercentage = stats.passesAccuracyPercentage,
            goalsPrevented = stats.goalsPrevented,
            xg = stats.xg.map { toXG(it) },
        )

    private fun toXG(xg: FixtureStatisticsModel.XG): FixtureStatisticsResponse.XG =
        FixtureStatisticsResponse.XG(
            elapsed = xg.elapsed,
            xg = xg.xg,
        )

    private fun toPlayerWithStatistics(player: FixtureStatisticsModel.PlayerWithStatistics): FixtureStatisticsResponse.PlayerWithStatistics =
        FixtureStatisticsResponse.PlayerWithStatistics(
            player =
                FixtureStatisticsResponse.PlayerInfo(
                    matchPlayerUid = player.player.matchPlayerUid ?: "",
                    playerUid = player.player.playerUid,
                    name = player.player.name ?: "",
                    koreanName = player.player.koreanName,
                    photo = player.player.photo,
                    position = player.player.position,
                    number = player.player.number,
                ),
            statistics = toPlayerStatistics(player.statistics),
        )

    private fun toPlayerStatistics(stats: FixtureStatisticsModel.PlayerStatistics): FixtureStatisticsResponse.PlayerStatistics =
        FixtureStatisticsResponse.PlayerStatistics(
            minutesPlayed = stats.minutesPlayed,
            position = stats.position,
            rating = stats.rating,
            captain = stats.captain,
            substitute = stats.substitute,
            shotsTotal = stats.shotsTotal,
            shotsOn = stats.shotsOn,
            goals = stats.goals,
            goalsConceded = stats.goalsConceded,
            assists = stats.assists,
            saves = stats.saves,
            passesTotal = stats.passesTotal,
            passesKey = stats.passesKey,
            passesAccuracy = stats.passesAccuracy,
            tacklesTotal = stats.tacklesTotal,
            interceptions = stats.interceptions,
            duelsTotal = stats.duelsTotal,
            duelsWon = stats.duelsWon,
            dribblesAttempts = stats.dribblesAttempts,
            dribblesSuccess = stats.dribblesSuccess,
            foulsCommitted = stats.foulsCommitted,
            foulsDrawn = stats.foulsDrawn,
            yellowCards = stats.yellowCards,
            redCards = stats.redCards,
            penaltiesScored = stats.penaltiesScored,
            penaltiesMissed = stats.penaltiesMissed,
            penaltiesSaved = stats.penaltiesSaved,
        )
}
