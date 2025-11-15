package com.footballay.core.web.football.mapper

import com.footballay.core.web.football.dto.*
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchEvent
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam
import com.footballay.core.logger
import org.springframework.stereotype.Component
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Match Data Entity → DTO 변환 Mapper
 *
 * **책임:**
 * - Entity를 DTO로 안전하게 변환
 * - null 안전성 보장
 * - 날짜/시간 포맷팅
 */
@Component
class MatchDataMapper {
    private val log = logger()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private val seoulZone = ZoneId.of("Asia/Seoul")

    /**
     * Fixture 기본 정보 변환
     */
    fun toFixtureInfoDto(fixture: FixtureApiSports): FixtureInfoDto {
        val core = fixture.core ?: throw IllegalArgumentException("FixtureCore is null")
        val season = fixture.season ?: throw IllegalArgumentException("Season is null")
        val league = season.leagueApiSports ?: throw IllegalArgumentException("League is null")
        val leagueCore = league.leagueCore ?: throw IllegalArgumentException("LeagueCore is null")

        val homeTeamCore = core.homeTeam ?: throw IllegalArgumentException("Home team is null")
        val awayTeamCore = core.awayTeam ?: throw IllegalArgumentException("Away team is null")

        val dateStr =
            fixture.date
                ?.atZone(seoulZone)
                ?.format(dateFormatter)
                ?: ""

        return FixtureInfoDto(
            fixtureUid = core.uid,
            referee = fixture.referee,
            date = dateStr,
            league =
                FixtureInfoDto.LeagueInfo(
                    id = leagueCore.id ?: 0L,
                    name = league.name, // LeagueApiSports.name
                    koreanName = null, // koreanName 필드 없음
                    logo = league.logo,
                ),
            home =
                FixtureInfoDto.TeamInfo(
                    id = homeTeamCore.id ?: 0L,
                    name = homeTeamCore.name,
                    koreanName = null, // koreanName 필드 없음
                    logo = homeTeamCore.teamApiSports?.logo,
                ),
            away =
                FixtureInfoDto.TeamInfo(
                    id = awayTeamCore.id ?: 0L,
                    name = awayTeamCore.name,
                    koreanName = null, // koreanName 필드 없음
                    logo = awayTeamCore.teamApiSports?.logo,
                ),
        )
    }

    /**
     * Fixture 라이브 상태 변환
     */
    fun toFixtureLiveStatusDto(fixture: FixtureApiSports): FixtureLiveStatusDto {
        val core = fixture.core ?: throw IllegalArgumentException("FixtureCore is null")
        val status = fixture.status
        val score = fixture.score

        return FixtureLiveStatusDto(
            fixtureUid = core.uid,
            liveStatus =
                FixtureLiveStatusDto.LiveStatus(
                    elapsed = status?.elapsed,
                    shortStatus = status?.shortStatus ?: "",
                    longStatus = status?.longStatus ?: "",
                    score =
                        FixtureLiveStatusDto.Score(
                            home = score?.totalHome,
                            away = score?.totalAway,
                        ),
                ),
        )
    }

    /**
     * 경기 이벤트 목록 변환
     */
    fun toFixtureEventsDto(
        fixtureUid: String,
        events: List<ApiSportsMatchEvent>,
    ): FixtureEventsDto =
        FixtureEventsDto(
            fixtureUid = fixtureUid,
            events = events.map { toEventInfo(it) },
        )

    private fun toEventInfo(event: ApiSportsMatchEvent): FixtureEventsDto.EventInfo {
        val matchTeam = event.matchTeam
        val teamApiSports = matchTeam?.teamApiSports
        val teamCore = teamApiSports?.teamCore

        return FixtureEventsDto.EventInfo(
            sequence = event.sequence,
            elapsed = event.elapsedTime,
            extraTime = event.extraTime,
            team =
                FixtureEventsDto.TeamInfo(
                    teamId = teamCore?.id ?: 0L,
                    name = teamCore?.name ?: "",
                    koreanName = null, // koreanName 필드 없음
                ),
            player = event.player?.let { toPlayerInfo(it) },
            assist = event.assist?.let { toPlayerInfo(it) },
            type = event.eventType,
            detail = event.detail ?: "",
            comments = event.comments,
        )
    }

    private fun toPlayerInfo(matchPlayer: ApiSportsMatchPlayer): FixtureEventsDto.PlayerInfo {
        val playerApiSports = matchPlayer.playerApiSports
        val playerCore = playerApiSports?.playerCore

        return FixtureEventsDto.PlayerInfo(
            playerId = playerCore?.id,
            name = matchPlayer.name ?: playerCore?.name,
            koreanName = null, // koreanName 필드 없음
            number = matchPlayer.number,
            tempId = matchPlayer.matchPlayerUid,
        )
    }

    /**
     * 경기 라인업 변환
     */
    fun toFixtureLineupDto(
        fixtureUid: String,
        homeTeamFixture: FixtureApiSports?,
        awayTeamFixture: FixtureApiSports?,
    ): FixtureLineupDto {
        val homeLineup =
            homeTeamFixture?.homeTeam?.let { toStartLineup(it) }
                ?: createEmptyLineup()
        val awayLineup =
            awayTeamFixture?.awayTeam?.let { toStartLineup(it) }
                ?: createEmptyLineup()

        return FixtureLineupDto(
            fixtureUid = fixtureUid,
            lineup =
                FixtureLineupDto.Lineup(
                    home = homeLineup,
                    away = awayLineup,
                ),
        )
    }

    private fun toStartLineup(matchTeam: ApiSportsMatchTeam): FixtureLineupDto.StartLineup {
        val teamApiSports = matchTeam.teamApiSports
        val teamCore = teamApiSports?.teamCore

        val allPlayers = matchTeam.players ?: emptyList()
        val startXI = allPlayers.filter { !it.substitute }.map { toLineupPlayer(it) }
        val substitutes = allPlayers.filter { it.substitute }.map { toLineupPlayer(it) }

        return FixtureLineupDto.StartLineup(
            teamId = teamCore?.id ?: 0L,
            teamName = teamCore?.name ?: "",
            teamKoreanName = null, // koreanName 필드 없음
            formation = matchTeam.formation,
            players = startXI,
            substitutes = substitutes,
        )
    }

    private fun toLineupPlayer(matchPlayer: ApiSportsMatchPlayer): FixtureLineupDto.LineupPlayer {
        val playerApiSports = matchPlayer.playerApiSports
        val playerCore = playerApiSports?.playerCore

        return FixtureLineupDto.LineupPlayer(
            id = playerCore?.id ?: 0L,
            name = matchPlayer.name ?: playerCore?.name ?: "",
            koreanName = null, // koreanName 필드 없음
            number = matchPlayer.number,
            photo = playerApiSports?.photo,
            position = matchPlayer.position,
            grid = matchPlayer.grid,
            substitute = matchPlayer.substitute,
            tempId = matchPlayer.matchPlayerUid,
        )
    }

    private fun createEmptyLineup() =
        FixtureLineupDto.StartLineup(
            teamId = 0L,
            teamName = "",
            teamKoreanName = null,
            formation = null,
            players = emptyList(),
            substitutes = emptyList(),
        )

    /**
     * 경기 통계 변환
     */
    fun toFixtureStatisticsDto(
        fixtureUid: String,
        fixture: FixtureApiSports,
        homeTeamFixture: FixtureApiSports?,
        awayTeamFixture: FixtureApiSports?,
    ): FixtureStatisticsDto {
        val status = fixture.status

        return FixtureStatisticsDto(
            fixture =
                FixtureStatisticsDto.FixtureBasic(
                    uid = fixtureUid,
                    elapsed = status?.elapsed,
                    status = status?.shortStatus ?: "",
                ),
            home =
                homeTeamFixture?.homeTeam?.let { toTeamWithStatistics(it) }
                    ?: createEmptyTeamStatistics(),
            away =
                awayTeamFixture?.awayTeam?.let { toTeamWithStatistics(it) }
                    ?: createEmptyTeamStatistics(),
        )
    }

    private fun toTeamWithStatistics(matchTeam: ApiSportsMatchTeam): FixtureStatisticsDto.TeamWithStatistics {
        val teamApiSports = matchTeam.teamApiSports
        val teamCore = teamApiSports?.teamCore
        val teamStats = matchTeam.teamStatistics

        return FixtureStatisticsDto.TeamWithStatistics(
            team =
                FixtureStatisticsDto.TeamInfo(
                    id = teamCore?.id ?: 0L,
                    name = teamCore?.name ?: "",
                    koreanName = null, // koreanName 필드 없음
                    logo = teamApiSports?.logo,
                ),
            teamStatistics =
                FixtureStatisticsDto.TeamStatistics(
                    shotsOnGoal = teamStats?.shotsOnGoal ?: 0,
                    shotsOffGoal = teamStats?.shotsOffGoal ?: 0,
                    totalShots = teamStats?.totalShots ?: 0,
                    blockedShots = teamStats?.blockedShots ?: 0,
                    shotsInsideBox = teamStats?.shotsInsideBox ?: 0,
                    shotsOutsideBox = teamStats?.shotsOutsideBox ?: 0,
                    fouls = teamStats?.fouls ?: 0,
                    cornerKicks = teamStats?.cornerKicks ?: 0,
                    offsides = teamStats?.offsides ?: 0,
                    ballPossession = parseIntFromPercentage(teamStats?.ballPossession),
                    yellowCards = teamStats?.yellowCards ?: 0,
                    redCards = teamStats?.redCards ?: 0,
                    goalkeeperSaves = teamStats?.goalkeeperSaves ?: 0,
                    totalPasses = teamStats?.totalPasses ?: 0,
                    passesAccurate = teamStats?.passesAccurate ?: 0,
                    passesAccuracyPercentage = parseIntFromPercentage(teamStats?.passesPercentage),
                    goalsPrevented = teamStats?.goalsPrevented ?: 0,
                    xg =
                        teamStats?.xgList?.map {
                            FixtureStatisticsDto.XG(
                                elapsed = it.elapsedTime,
                                xg = it.expectedGoals.toString(),
                            )
                        } ?: emptyList(),
                ),
            playerStatistics =
                matchTeam.players?.mapNotNull { toPlayerWithStatistics(it) } ?: emptyList(),
        )
    }

    private fun toPlayerWithStatistics(matchPlayer: ApiSportsMatchPlayer): FixtureStatisticsDto.PlayerWithStatistics? {
        val playerStats = matchPlayer.statistics ?: return null
        val playerApiSports = matchPlayer.playerApiSports
        val playerCore = playerApiSports?.playerCore

        return FixtureStatisticsDto.PlayerWithStatistics(
            player =
                FixtureStatisticsDto.PlayerInfoBasic(
                    id = playerCore?.id,
                    name = matchPlayer.name ?: playerCore?.name,
                    koreanName = null, // koreanName 필드 없음
                    photo = playerApiSports?.photo,
                    position = matchPlayer.position,
                    number = matchPlayer.number,
                    tempId = matchPlayer.matchPlayerUid,
                ),
            statistics =
                FixtureStatisticsDto.PlayerStatistics(
                    minutesPlayed = playerStats.minutesPlayed ?: 0,
                    position = matchPlayer.position,
                    rating = playerStats.rating?.toString(),
                    captain = playerStats.isCaptain,
                    substitute = matchPlayer.substitute,
                    shotsTotal = playerStats.shotsTotal ?: 0,
                    shotsOn = playerStats.shotsOnTarget ?: 0,
                    goals = playerStats.goalsTotal ?: 0,
                    goalsConceded = playerStats.goalsConceded ?: 0,
                    assists = playerStats.assists ?: 0,
                    saves = playerStats.saves ?: 0,
                    passesTotal = playerStats.passesTotal ?: 0,
                    passesKey = playerStats.keyPasses ?: 0,
                    passesAccuracy = playerStats.passesAccuracy ?: 0,
                    tacklesTotal = playerStats.tacklesTotal ?: 0,
                    interceptions = playerStats.interceptions ?: 0,
                    duelsTotal = playerStats.duelsTotal ?: 0,
                    duelsWon = playerStats.duelsWon ?: 0,
                    dribblesAttempts = playerStats.dribblesAttempts ?: 0,
                    dribblesSuccess = playerStats.dribblesSuccess ?: 0,
                    foulsCommitted = playerStats.foulsCommitted ?: 0,
                    foulsDrawn = playerStats.foulsDrawn ?: 0,
                    yellowCards = playerStats.yellowCards,
                    redCards = playerStats.redCards,
                    penaltiesScored = playerStats.penaltyScored,
                    penaltiesMissed = playerStats.penaltyMissed,
                    penaltiesSaved = playerStats.penaltySaved,
                ),
        )
    }

    private fun createEmptyTeamStatistics() =
        FixtureStatisticsDto.TeamWithStatistics(
            team =
                FixtureStatisticsDto.TeamInfo(
                    id = 0L,
                    name = "",
                    koreanName = null,
                    logo = null,
                ),
            teamStatistics =
                FixtureStatisticsDto.TeamStatistics(
                    shotsOnGoal = 0,
                    shotsOffGoal = 0,
                    totalShots = 0,
                    blockedShots = 0,
                    shotsInsideBox = 0,
                    shotsOutsideBox = 0,
                    fouls = 0,
                    cornerKicks = 0,
                    offsides = 0,
                    ballPossession = 0,
                    yellowCards = 0,
                    redCards = 0,
                    goalkeeperSaves = 0,
                    totalPasses = 0,
                    passesAccurate = 0,
                    passesAccuracyPercentage = 0,
                    goalsPrevented = 0,
                    xg = emptyList(),
                ),
            playerStatistics = emptyList(),
        )

    /**
     * 퍼센티지 문자열을 Int로 변환
     * 예: "67%" → 67, null → 0
     */
    private fun parseIntFromPercentage(percentageStr: String?): Int = percentageStr?.removeSuffix("%")?.toIntOrNull() ?: 0
}
