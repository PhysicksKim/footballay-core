package com.footballay.core.infra.query

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.model.match.*
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchEvent
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam
import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.live.ApiSportsMatchEventRepository
import com.footballay.core.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Match Data 조회 서비스
 *
 * FixtureUid 기반으로 라이브 매치 데이터를 조회합니다.
 * - uid → entity 조회
 */
@Service
@Transactional(readOnly = true)
class MatchDataQueryServiceImpl(
    private val fixtureApiSportsRepository: FixtureApiSportsRepository,
    private val matchEventRepository: ApiSportsMatchEventRepository,
) : MatchDataQueryService {
    private val log = logger()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private val seoulZone = ZoneId.of("Asia/Seoul")

    /**
     * Fixture 기본 정보 조회
     *
     * @param fixtureUid Fixture UID
     * @return DomainResult<FixtureInfoModel>
     */
    override fun getFixtureInfo(fixtureUid: String): DomainResult<FixtureInfoModel, DomainFail> {
        val fixture =
            fixtureApiSportsRepository.findByCoreUid(fixtureUid)
                ?: return DomainResult.Fail(DomainFail.NotFound("Fixture", fixtureUid))

        log.debug("Fetched fixture info for uid: {}", fixtureUid)

        return try {
            val model = toFixtureInfoModel(fixture)
            DomainResult.Success(model)
        } catch (e: Exception) {
            log.error("Error converting fixture to domain model for uid: {}", fixtureUid, e)
            DomainResult.Fail(
                DomainFail.Validation.single(
                    code = "FIXTURE_CONVERSION_ERROR",
                    message = "Failed to convert fixture data: ${e.message}",
                ),
            )
        }
    }

    /**
     * Fixture 라이브 상태 조회 (스코어, 경기 시간, 상태)
     *
     * @param fixtureUid Fixture UID
     * @return DomainResult<FixtureLiveStatusModel>
     */
    override fun getFixtureLiveStatus(fixtureUid: String): DomainResult<FixtureLiveStatusModel, DomainFail> {
        val fixture =
            fixtureApiSportsRepository.findByCoreUid(fixtureUid)
                ?: return DomainResult.Fail(DomainFail.NotFound("Fixture", fixtureUid))

        log.debug("Fetched fixture live status for uid: {}", fixtureUid)

        return try {
            val model = toFixtureLiveStatusModel(fixture)
            DomainResult.Success(model)
        } catch (e: Exception) {
            log.error("Error converting fixture live status for uid: {}", fixtureUid, e)
            DomainResult.Fail(
                DomainFail.Validation.single(
                    code = "FIXTURE_LIVE_STATUS_CONVERSION_ERROR",
                    message = "Failed to convert fixture live status: ${e.message}",
                ),
            )
        }
    }

    /**
     * 경기 이벤트 조회 (골, 카드, 교체 등)
     *
     * @param fixtureUid Fixture UID
     * @return DomainResult<FixtureEventsModel>
     */
    override fun getFixtureEvents(fixtureUid: String): DomainResult<FixtureEventsModel, DomainFail> {
        // FixtureApiSports를 먼저 조회해서 존재 여부 확인
        val fixture =
            fixtureApiSportsRepository.findByCoreUid(fixtureUid)
                ?: return DomainResult.Fail(DomainFail.NotFound("Fixture", fixtureUid))

        val events = matchEventRepository.findByFixtureUidOrderBySequenceAsc(fixtureUid)
        log.debug("Fetched {} events for fixture uid: {}", events.size, fixtureUid)

        return try {
            val model = toFixtureEventsModel(fixtureUid, events)
            DomainResult.Success(model)
        } catch (e: Exception) {
            log.error("Error converting fixture events for uid: {}", fixtureUid, e)
            DomainResult.Fail(
                DomainFail.Validation.single(
                    code = "FIXTURE_EVENTS_CONVERSION_ERROR",
                    message = "Failed to convert fixture events: ${e.message}",
                ),
            )
        }
    }

    /**
     * 경기 라인업 조회 (홈/원정 선수 정보)
     *
     * 통계 제외한 가벼운 쿼리로 라인업만 조회
     *
     * @param fixtureUid Fixture UID
     * @return DomainResult<FixtureLineupModel>
     */
    override fun getFixtureLineup(fixtureUid: String): DomainResult<FixtureLineupModel, DomainFail> {
        val homeTeam = fixtureApiSportsRepository.findFixtureHomeTeamLineupByUid(fixtureUid)
        val awayTeam = fixtureApiSportsRepository.findFixtureAwayTeamLineupByUid(fixtureUid)

        if (homeTeam == null && awayTeam == null) {
            return DomainResult.Fail(DomainFail.NotFound("Fixture", fixtureUid))
        }

        log.debug("Fetched lineup for fixture uid: {}", fixtureUid)

        return try {
            val model = toFixtureLineupModel(fixtureUid, homeTeam, awayTeam)
            DomainResult.Success(model)
        } catch (e: Exception) {
            log.error("Error converting fixture lineup for uid: {}", fixtureUid, e)
            DomainResult.Fail(
                DomainFail.Validation.single(
                    code = "FIXTURE_LINEUP_CONVERSION_ERROR",
                    message = "Failed to convert fixture lineup: ${e.message}",
                ),
            )
        }
    }

    /**
     * 경기 통계 조회 (팀/선수별 통계)
     *
     * @param fixtureUid Fixture UID
     * @return DomainResult<FixtureStatisticsModel>
     */
    override fun getFixtureStatistics(fixtureUid: String): DomainResult<FixtureStatisticsModel, DomainFail> {
        // Fixture 기본 정보 조회 (상태, elapsed 등)
        val fixture =
            fixtureApiSportsRepository.findByCoreUid(fixtureUid)
                ?: return DomainResult.Fail(DomainFail.NotFound("Fixture", fixtureUid))

        // 홈/원정 팀 통계 조회
        val homeTeam = fixtureApiSportsRepository.findFixtureHomeTeamLineupAndStatsByUid(fixtureUid)
        val awayTeam = fixtureApiSportsRepository.findFixtureAwayTeamLineupAndStatsByUid(fixtureUid)

        log.debug("Fetched statistics for fixture uid: {}", fixtureUid)

        return try {
            val model = toFixtureStatisticsModel(fixtureUid, fixture, homeTeam, awayTeam)
            DomainResult.Success(model)
        } catch (e: Exception) {
            log.error("Error converting fixture statistics for uid: {}", fixtureUid, e)
            DomainResult.Fail(
                DomainFail.Validation.single(
                    code = "FIXTURE_STATISTICS_CONVERSION_ERROR",
                    message = "Failed to convert fixture statistics: ${e.message}",
                ),
            )
        }
    }

    // ===========================
    // Entity → Domain Model 변환 메서드
    // ===========================

    private fun toFixtureInfoModel(fixture: FixtureApiSports): FixtureInfoModel {
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

        return FixtureInfoModel(
            fixtureUid = core.uid,
            referee = fixture.referee,
            date = dateStr,
            league =
                FixtureInfoModel.LeagueInfo(
                    id = leagueCore.id ?: 0L,
                    name = league.name,
                    koreanName = null,
                    logo = league.logo,
                ),
            home =
                FixtureInfoModel.TeamInfo(
                    id = homeTeamCore.id ?: 0L,
                    name = homeTeamCore.name,
                    koreanName = null,
                    logo = homeTeamCore.teamApiSports?.logo,
                ),
            away =
                FixtureInfoModel.TeamInfo(
                    id = awayTeamCore.id ?: 0L,
                    name = awayTeamCore.name,
                    koreanName = null,
                    logo = awayTeamCore.teamApiSports?.logo,
                ),
        )
    }

    private fun toFixtureLiveStatusModel(fixture: FixtureApiSports): FixtureLiveStatusModel {
        val core = fixture.core ?: throw IllegalArgumentException("FixtureCore is null")
        val status = fixture.status
        val score = fixture.score

        return FixtureLiveStatusModel(
            fixtureUid = core.uid,
            liveStatus =
                FixtureLiveStatusModel.LiveStatus(
                    elapsed = status?.elapsed,
                    shortStatus = status?.shortStatus ?: "",
                    longStatus = status?.longStatus ?: "",
                    score =
                        FixtureLiveStatusModel.Score(
                            home = score?.totalHome,
                            away = score?.totalAway,
                        ),
                ),
        )
    }

    private fun toFixtureEventsModel(
        fixtureUid: String,
        events: List<ApiSportsMatchEvent>,
    ): FixtureEventsModel =
        FixtureEventsModel(
            fixtureUid = fixtureUid,
            events = events.map { toEventInfo(it) },
        )

    private fun toEventInfo(event: ApiSportsMatchEvent): FixtureEventsModel.EventInfo {
        val matchTeam = event.matchTeam
        val teamApiSports = matchTeam?.teamApiSports
        val teamCore = teamApiSports?.teamCore

        return FixtureEventsModel.EventInfo(
            sequence = event.sequence,
            elapsed = event.elapsedTime,
            extraTime = event.extraTime,
            team =
                FixtureEventsModel.TeamInfo(
                    teamId = teamCore?.id ?: 0L,
                    name = teamCore?.name ?: "",
                    koreanName = null,
                ),
            player = event.player?.let { toPlayerInfo(it) },
            assist = event.assist?.let { toPlayerInfo(it) },
            type = event.eventType,
            detail = event.detail ?: "",
            comments = event.comments,
        )
    }

    private fun toPlayerInfo(matchPlayer: ApiSportsMatchPlayer): FixtureEventsModel.PlayerInfo {
        val playerApiSports = matchPlayer.playerApiSports
        val playerCore = playerApiSports?.playerCore

        return FixtureEventsModel.PlayerInfo(
            playerId = playerCore?.id,
            name = matchPlayer.name ?: playerCore?.name,
            koreanName = null,
            number = matchPlayer.number,
            tempId = matchPlayer.matchPlayerUid,
        )
    }

    private fun toFixtureLineupModel(
        fixtureUid: String,
        homeTeamFixture: FixtureApiSports?,
        awayTeamFixture: FixtureApiSports?,
    ): FixtureLineupModel {
        val homeLineup =
            homeTeamFixture?.homeTeam?.let { toStartLineup(it) }
                ?: createEmptyLineup()
        val awayLineup =
            awayTeamFixture?.awayTeam?.let { toStartLineup(it) }
                ?: createEmptyLineup()

        return FixtureLineupModel(
            fixtureUid = fixtureUid,
            lineup =
                FixtureLineupModel.Lineup(
                    home = homeLineup,
                    away = awayLineup,
                ),
        )
    }

    private fun toStartLineup(matchTeam: ApiSportsMatchTeam): FixtureLineupModel.StartLineup {
        val teamApiSports = matchTeam.teamApiSports
        val teamCore = teamApiSports?.teamCore

        val allPlayers = matchTeam.players ?: emptyList()
        val startXI = allPlayers.filter { !it.substitute }.map { toLineupPlayer(it) }
        val substitutes = allPlayers.filter { it.substitute }.map { toLineupPlayer(it) }

        return FixtureLineupModel.StartLineup(
            teamId = teamCore?.id ?: 0L,
            teamName = teamCore?.name ?: "",
            teamKoreanName = null,
            formation = matchTeam.formation,
            players = startXI,
            substitutes = substitutes,
        )
    }

    private fun toLineupPlayer(matchPlayer: ApiSportsMatchPlayer): FixtureLineupModel.LineupPlayer {
        val playerApiSports = matchPlayer.playerApiSports
        val playerCore = playerApiSports?.playerCore

        return FixtureLineupModel.LineupPlayer(
            id = playerCore?.id ?: 0L,
            name = matchPlayer.name ?: playerCore?.name ?: "",
            koreanName = null,
            number = matchPlayer.number,
            photo = playerApiSports?.photo,
            position = matchPlayer.position,
            grid = matchPlayer.grid,
            substitute = matchPlayer.substitute,
            tempId = matchPlayer.matchPlayerUid,
        )
    }

    private fun createEmptyLineup() =
        FixtureLineupModel.StartLineup(
            teamId = 0L,
            teamName = "",
            teamKoreanName = null,
            formation = null,
            players = emptyList(),
            substitutes = emptyList(),
        )

    private fun toFixtureStatisticsModel(
        fixtureUid: String,
        fixture: FixtureApiSports,
        homeTeamFixture: FixtureApiSports?,
        awayTeamFixture: FixtureApiSports?,
    ): FixtureStatisticsModel {
        val status = fixture.status

        return FixtureStatisticsModel(
            fixture =
                FixtureStatisticsModel.FixtureBasic(
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

    private fun toTeamWithStatistics(matchTeam: ApiSportsMatchTeam): FixtureStatisticsModel.TeamWithStatistics {
        val teamApiSports = matchTeam.teamApiSports
        val teamCore = teamApiSports?.teamCore
        val teamStats = matchTeam.teamStatistics

        return FixtureStatisticsModel.TeamWithStatistics(
            team =
                FixtureStatisticsModel.TeamInfo(
                    id = teamCore?.id ?: 0L,
                    name = teamCore?.name ?: "",
                    koreanName = null,
                    logo = teamApiSports?.logo,
                ),
            teamStatistics =
                FixtureStatisticsModel.TeamStatistics(
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
                            FixtureStatisticsModel.XG(
                                elapsed = it.elapsedTime,
                                xg = it.expectedGoals.toString(),
                            )
                        } ?: emptyList(),
                ),
            playerStatistics =
                matchTeam.players?.mapNotNull { toPlayerWithStatistics(it) } ?: emptyList(),
        )
    }

    private fun toPlayerWithStatistics(matchPlayer: ApiSportsMatchPlayer): FixtureStatisticsModel.PlayerWithStatistics? {
        val playerStats = matchPlayer.statistics ?: return null
        val playerApiSports = matchPlayer.playerApiSports
        val playerCore = playerApiSports?.playerCore

        return FixtureStatisticsModel.PlayerWithStatistics(
            player =
                FixtureStatisticsModel.PlayerInfoBasic(
                    id = playerCore?.id,
                    name = matchPlayer.name ?: playerCore?.name,
                    koreanName = null,
                    photo = playerApiSports?.photo,
                    position = matchPlayer.position,
                    number = matchPlayer.number,
                    tempId = matchPlayer.matchPlayerUid,
                ),
            statistics =
                FixtureStatisticsModel.PlayerStatistics(
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
        FixtureStatisticsModel.TeamWithStatistics(
            team =
                FixtureStatisticsModel.TeamInfo(
                    id = 0L,
                    name = "",
                    koreanName = null,
                    logo = null,
                ),
            teamStatistics =
                FixtureStatisticsModel.TeamStatistics(
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

    private fun parseIntFromPercentage(percentageStr: String?): Int = percentageStr?.removeSuffix("%")?.toIntOrNull() ?: 0
}
