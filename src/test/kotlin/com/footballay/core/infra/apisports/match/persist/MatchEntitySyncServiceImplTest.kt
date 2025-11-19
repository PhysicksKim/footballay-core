package com.footballay.core.infra.apisports.match.persist

import com.footballay.core.infra.apisports.match.MatchEntityPersistManagerImpl
import com.footballay.core.infra.apisports.match.plan.context.MatchEntityBundle
import com.footballay.core.infra.apisports.match.plan.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.plan.dto.FixtureApiSportsDto
import com.footballay.core.infra.apisports.match.plan.dto.MatchLineupPlanDto
import com.footballay.core.infra.apisports.match.plan.dto.MatchEventDto
import com.footballay.core.infra.apisports.match.plan.dto.MatchEventPlanDto
import com.footballay.core.infra.apisports.match.plan.dto.MatchPlayerDto
import com.footballay.core.infra.apisports.match.plan.dto.MatchPlayerStatPlanDto
import com.footballay.core.infra.apisports.match.plan.dto.MatchTeamStatPlanDto
import com.footballay.core.infra.apisports.match.plan.loader.MatchDataLoader
import com.footballay.core.infra.apisports.match.persist.base.BaseMatchEntityManager
import com.footballay.core.infra.apisports.match.persist.base.BaseMatchSyncResult
import com.footballay.core.infra.apisports.match.persist.event.manager.MatchEventManager
import com.footballay.core.infra.apisports.match.persist.event.manager.MatchEventProcessResult
import com.footballay.core.infra.apisports.match.persist.player.manager.MatchPlayerManager
import com.footballay.core.infra.apisports.match.persist.player.manager.MatchPlayerProcessResult
import com.footballay.core.infra.apisports.match.persist.playerstat.manager.PlayerStatsManager
import com.footballay.core.infra.apisports.match.persist.playerstat.result.PlayerStatsProcessResult
import com.footballay.core.infra.apisports.match.persist.teamstat.manager.TeamStatsManager
import com.footballay.core.infra.apisports.match.persist.teamstat.result.TeamStatsProcessResult
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsSeason
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchEvent
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayerStatistics
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class MatchEntitySyncServiceImplTest {
    private lateinit var matchEntitySyncService: MatchEntityPersistManagerImpl
    private lateinit var matchDataLoader: MatchDataLoader
    private lateinit var baseMatchEntityManager: BaseMatchEntityManager
    private lateinit var matchPlayerManager: MatchPlayerManager
    private lateinit var matchEventManager: MatchEventManager
    private lateinit var playerStatsManager: PlayerStatsManager
    private lateinit var teamStatsManager: TeamStatsManager

    @BeforeEach
    fun setUp() {
        matchDataLoader = mock()
        baseMatchEntityManager = mock()
        matchPlayerManager = mock()
        matchEventManager = mock()
        playerStatsManager = mock()
        teamStatsManager = mock()

        matchEntitySyncService =
            MatchEntityPersistManagerImpl(
                matchDataLoader,
                baseMatchEntityManager,
                matchPlayerManager,
                matchEventManager,
                playerStatsManager,
                teamStatsManager,
            )
    }

    @Test
    @DisplayName("모든 처리 단계가 성공적으로 완료되어 Match 관련 엔티티가 정상적으로 동기화됩니다")
    fun testSuccessfulEntitySync() {
        // given
        val fixtureApiId = 12345L
        val baseDto = createMockFixtureDto()
        val lineupDto = createMockLineupDto()
        val eventDto = createMockEventDto()
        val teamStatDto = createMockTeamStatDto()
        val playerStatDto = createMockPlayerStatDto()
        val playerContext = createMockPlayerContext()

        val entityBundle = MatchEntityBundle.createEmpty()
        val savedPlayers =
            listOf(
                createMockMatchPlayer("Player 1"),
                createMockMatchPlayer("Player 2"),
            )

        // Mock 설정
        whenever(matchDataLoader.loadContext(eq(fixtureApiId), any(), any())).then { }
        whenever(
            baseMatchEntityManager.syncBaseEntities(
                eq(fixtureApiId),
                any(),
                any(),
            ),
        ).thenReturn(
            BaseMatchSyncResult.success(
                fixture = createMockFixture(),
                homeMatchTeam = createMockMatchTeam("Home Team"),
                awayMatchTeam = createMockMatchTeam("Away Team"),
            ),
        )
        whenever(matchPlayerManager.processMatchTeamAndPlayers(any(), any(), any())).thenReturn(
            MatchPlayerProcessResult(
                totalPlayers = 2,
                createdCount = 2,
                updatedCount = 0,
                deletedCount = 0,
                savedPlayers = savedPlayers,
            ),
        )
        whenever(matchEventManager.processMatchEvents(any(), any())).thenReturn(
            MatchEventProcessResult(
                totalEvents = 1,
                createdCount = 1,
                updatedCount = 0,
                deletedCount = 0,
                savedEvents = listOf(createMockMatchEvent()),
            ),
        )
        val savedPlayerStats =
            listOf(
                createMockPlayerStats("Player 1"),
                createMockPlayerStats("Player 2"),
            )
        whenever(playerStatsManager.processPlayerStats(any(), any())).thenReturn(
            PlayerStatsProcessResult(
                totalStats = 2,
                createdCount = 2,
                updatedCount = 0,
                deletedCount = 0,
                savedStats = savedPlayerStats,
            ),
        )
        whenever(teamStatsManager.processTeamStats(any(), any())).thenReturn(
            TeamStatsProcessResult(
                hasHome = true,
                hasAway = true,
                createdCount = 2,
                updatedCount = 0,
                homeTeamStat = null,
                awayTeamStat = null,
            ),
        )

        // when
        val result =
            matchEntitySyncService.syncMatchEntities(
                fixtureApiId,
                baseDto,
                lineupDto,
                eventDto,
                teamStatDto,
                playerStatDto,
                playerContext,
            )

        // then
        assertThat(result.success).isTrue()
        assertThat(result.createdCount).isEqualTo(7) // 2 players + 1 event + 2 playerStats + 2 teamStats
        assertThat(result.updatedCount).isEqualTo(0)
        assertThat(result.deletedCount).isEqualTo(0)
        assertThat(result.playerChanges.created).isEqualTo(2)
        assertThat(result.eventChanges.created).isEqualTo(1)

        // 모든 매니저 호출 검증
        verify(matchPlayerManager).processMatchTeamAndPlayers(any(), any(), any())
        verify(matchEventManager).processMatchEvents(any(), any())
        verify(playerStatsManager).processPlayerStats(any(), any())
        verify(teamStatsManager).processTeamStats(any(), any())
        verify(matchDataLoader).loadContext(eq(fixtureApiId), any(), any())
        verify(baseMatchEntityManager).syncBaseEntities(eq(fixtureApiId), any(), any())
    }

    @Test
    @DisplayName("Lineup 저장 시 예외가 발생하면 전체 동기화가 실패하고 에러 메시지를 반환합니다")
    fun testLineupErrorHandling() {
        // given
        val fixtureApiId = 12345L
        val baseDto = createMockFixtureDto()
        val lineupDto = createMockLineupDto()
        val eventDto = createMockEventDto()
        val teamStatDto = createMockTeamStatDto()
        val playerStatDto = createMockPlayerStatDto()
        val playerContext = createMockPlayerContext()

        // Mock 설정 - MatchPlayerManager에서 예외 발생
        whenever(matchDataLoader.loadContext(eq(fixtureApiId), any(), any())).then { }
        whenever(
            baseMatchEntityManager.syncBaseEntities(
                eq(fixtureApiId),
                any(),
                any(),
            ),
        ).thenReturn(
            BaseMatchSyncResult.success(
                fixture = createMockFixture(),
                homeMatchTeam = createMockMatchTeam("Home Team"),
                awayMatchTeam = createMockMatchTeam("Away Team"),
            ),
        )
        whenever(matchPlayerManager.processMatchTeamAndPlayers(any(), any(), any())).thenThrow(
            RuntimeException("MatchPlayer processing failed"),
        )

        // when
        val result =
            matchEntitySyncService.syncMatchEntities(
                fixtureApiId,
                baseDto,
                lineupDto,
                eventDto,
                teamStatDto,
                playerStatDto,
                playerContext,
            )

        // then
        assertThat(result.success).isFalse()
    }

    @Test
    @DisplayName("Base 엔티티 동기화가 실패하면 다음 처리가 실행되지 않고 즉시 실패 결과를 반환합니다")
    fun testBaseEntitySyncFailure() {
        // given
        val fixtureApiId = 12345L
        val baseDto = createMockFixtureDto()
        val lineupDto = createMockLineupDto()
        val eventDto = createMockEventDto()
        val teamStatDto = createMockTeamStatDto()
        val playerStatDto = createMockPlayerStatDto()
        val playerContext = createMockPlayerContext()

        // Mock 설정 - Base 엔티티 동기화 실패
        whenever(matchDataLoader.loadContext(eq(fixtureApiId), any(), any())).then { }
        whenever(
            baseMatchEntityManager.syncBaseEntities(
                eq(fixtureApiId),
                any(),
                any(),
            ),
        ).thenReturn(BaseMatchSyncResult.failure("Base entity sync failed"))

        // when
        val result =
            matchEntitySyncService.syncMatchEntities(
                fixtureApiId,
                baseDto,
                lineupDto,
                eventDto,
                teamStatDto,
                playerStatDto,
                playerContext,
            )

        // then
        assertThat(result.success).isFalse()

        // 다음 단계(MatchPlayer)가 실행되지 않았는지 확인
        verify(matchPlayerManager, never()).processMatchTeamAndPlayers(any(), any(), any())
    }

    @Test
    @DisplayName("PlayerStats 처리가 성공적으로 완료됩니다")
    fun testPlayerStatsProcessing() {
        // given
        val fixtureApiId = 12345L
        val baseDto = createMockFixtureDto()
        val lineupDto = createMockLineupDto()
        val eventDto = createMockEventDto()
        val teamStatDto = createMockTeamStatDto()
        val playerStatDto = createMockPlayerStatDto()
        val playerContext = createMockPlayerContext()

        val entityBundle = MatchEntityBundle.createEmpty()
        val savedPlayerStats =
            listOf(
                createMockPlayerStats("Player 1"),
                createMockPlayerStats("Player 2"),
            )

        // Mock 설정
        whenever(matchDataLoader.loadContext(eq(fixtureApiId), any(), any())).then { }
        whenever(
            baseMatchEntityManager.syncBaseEntities(
                eq(fixtureApiId),
                any(),
                any(),
            ),
        ).thenReturn(
            BaseMatchSyncResult.success(
                fixture = createMockFixture(),
                homeMatchTeam = createMockMatchTeam("Home Team"),
                awayMatchTeam = createMockMatchTeam("Away Team"),
            ),
        )
        whenever(matchPlayerManager.processMatchTeamAndPlayers(any(), any(), any())).thenReturn(
            MatchPlayerProcessResult(
                totalPlayers = 2,
                createdCount = 2,
                updatedCount = 0,
                deletedCount = 0,
                savedPlayers =
                    listOf(
                        createMockMatchPlayer("Player 1"),
                        createMockMatchPlayer("Player 2"),
                    ),
            ),
        )
        whenever(matchEventManager.processMatchEvents(any(), any())).thenReturn(
            MatchEventProcessResult(
                totalEvents = 1,
                createdCount = 1,
                updatedCount = 0,
                deletedCount = 0,
                savedEvents = listOf(createMockMatchEvent()),
            ),
        )
        whenever(playerStatsManager.processPlayerStats(any(), any())).thenReturn(
            PlayerStatsProcessResult(
                totalStats = 2,
                createdCount = 2,
                updatedCount = 0,
                deletedCount = 0,
                savedStats = savedPlayerStats,
            ),
        )
        whenever(teamStatsManager.processTeamStats(any(), any())).thenReturn(
            TeamStatsProcessResult(
                hasHome = true,
                hasAway = true,
                createdCount = 2,
                updatedCount = 0,
                homeTeamStat = null,
                awayTeamStat = null,
            ),
        )

        // when
        val result =
            matchEntitySyncService.syncMatchEntities(
                fixtureApiId,
                baseDto,
                lineupDto,
                eventDto,
                teamStatDto,
                playerStatDto,
                playerContext,
            )

        // then
        assertThat(result.success).isTrue()
        assertThat(result.createdCount).isEqualTo(7) // 2 players + 1 event + 2 playerStats + 2 teamStats
        assertThat(result.updatedCount).isEqualTo(0)
        assertThat(result.deletedCount).isEqualTo(0)
        assertThat(result.playerChanges.created).isEqualTo(2)
        assertThat(result.eventChanges.created).isEqualTo(1)

        // PlayerStats 처리 결과 확인
        verify(playerStatsManager).processPlayerStats(any(), any())
        verify(teamStatsManager).processTeamStats(any(), any())

        // PlayerStats 결과가 전체 결과에 반영되었는지 확인
        assertThat(result.createdCount).isGreaterThanOrEqualTo(2) // PlayerStats 포함
    }

    @Test
    @DisplayName("PlayerStats 처리 중 예외가 발생해도 전체 프로세스가 실패하지 않습니다")
    fun testPlayerStatsProcessingException() {
        // given
        val fixtureApiId = 12345L
        val baseDto = createMockFixtureDto()
        val lineupDto = createMockLineupDto()
        val eventDto = createMockEventDto()
        val teamStatDto = createMockTeamStatDto()
        val playerStatDto = createMockPlayerStatDto()
        val playerContext = createMockPlayerContext()

        // Mock 설정
        whenever(matchDataLoader.loadContext(eq(fixtureApiId), any(), any())).then { }
        whenever(
            baseMatchEntityManager.syncBaseEntities(
                eq(fixtureApiId),
                any(),
                any(),
            ),
        ).thenReturn(
            BaseMatchSyncResult.success(
                fixture = createMockFixture(),
                homeMatchTeam = createMockMatchTeam("Home Team"),
                awayMatchTeam = createMockMatchTeam("Away Team"),
            ),
        )
        whenever(matchPlayerManager.processMatchTeamAndPlayers(any(), any(), any())).thenReturn(
            MatchPlayerProcessResult(
                totalPlayers = 2,
                createdCount = 2,
                updatedCount = 0,
                deletedCount = 0,
                savedPlayers =
                    listOf(
                        createMockMatchPlayer("Player 1"),
                        createMockMatchPlayer("Player 2"),
                    ),
            ),
        )
        whenever(matchEventManager.processMatchEvents(any(), any())).thenReturn(
            MatchEventProcessResult(
                totalEvents = 1,
                createdCount = 1,
                updatedCount = 0,
                deletedCount = 0,
                savedEvents = listOf(createMockMatchEvent()),
            ),
        )

        // PlayerStats 처리에서 예외 발생
        whenever(playerStatsManager.processPlayerStats(any(), any())).thenThrow(
            RuntimeException("PlayerStats processing failed"),
        )

        // when
        val result =
            matchEntitySyncService.syncMatchEntities(
                fixtureApiId,
                baseDto,
                lineupDto,
                eventDto,
                teamStatDto,
                playerStatDto,
                playerContext,
            )

        // then
        assertThat(result.success).isTrue()
    }

    // 헬퍼 메서드들
    private fun createMockFixtureDto(): FixtureApiSportsDto =
        FixtureApiSportsDto(
            apiId = 12345L,
            referee = "Test Referee",
            timezone = "UTC",
            date = java.time.Instant.now(),
            round = "Round 1",
            status =
                FixtureApiSportsDto.StatusDto(
                    longStatus = "Match Finished",
                    shortStatus = "FT",
                    elapsed = 90,
                    extra = null,
                ),
            score =
                FixtureApiSportsDto.ScoreDto(
                    totalHome = 2,
                    totalAway = 1,
                    halftimeHome = 1,
                    halftimeAway = 0,
                    fulltimeHome = 2,
                    fulltimeAway = 1,
                    extratimeHome = null,
                    extratimeAway = null,
                    penaltyHome = null,
                    penaltyAway = null,
                ),
            venue =
                FixtureApiSportsDto.VenueDto(
                    apiId = 1L,
                    name = "Stadium",
                    city = "City",
                ),
            seasonYear = 2024,
            homeTeam =
                FixtureApiSportsDto.BaseTeamDto(
                    apiId = 1L,
                    name = "Home Team",
                    logo = "home_logo.png",
                    winner = true,
                ),
            awayTeam =
                FixtureApiSportsDto.BaseTeamDto(
                    apiId = 2L,
                    name = "Away Team",
                    logo = "away_logo.png",
                    winner = false,
                ),
        )

    private fun createMockLineupDto(): MatchLineupPlanDto =
        MatchLineupPlanDto(
            home =
                MatchLineupPlanDto.Lineup(
                    teamApiId = 1L,
                    teamName = "Home Team",
                    teamLogo = "home_logo.png",
                    playerColor = MatchLineupPlanDto.Color("red", "white", "red"),
                    goalkeeperColor = MatchLineupPlanDto.Color("blue", "white", "blue"),
                    formation = "4-3-3",
                    startMpKeys = listOf("mp_id_1", "mp_id_2"),
                    subMpKeys = listOf("mp_id_3", "mp_id_4"),
                ),
            away =
                MatchLineupPlanDto.Lineup(
                    teamApiId = 2L,
                    teamName = "Away Team",
                    teamLogo = "away_logo.png",
                    playerColor = MatchLineupPlanDto.Color("green", "white", "green"),
                    goalkeeperColor = MatchLineupPlanDto.Color("yellow", "black", "yellow"),
                    formation = "4-4-2",
                    startMpKeys = listOf("mp_id_5", "mp_id_6"),
                    subMpKeys = listOf("mp_id_7", "mp_id_8"),
                ),
        )

    private fun createMockEventDto(): MatchEventPlanDto =
        MatchEventPlanDto(
            events =
                listOf(
                    MatchEventDto(
                        sequence = 1,
                        elapsedTime = 15,
                        extraTime = null,
                        eventType = "Goal",
                        detail = "Normal Goal",
                        comments = "Goal scored",
                        teamApiId = 1L,
                        playerMpKey = "mp_id_1",
                        assistMpKey = "mp_id_2",
                    ),
                ),
        )

    private fun createMockTeamStatDto(): MatchTeamStatPlanDto =
        MatchTeamStatPlanDto(
            homeStats = null,
            awayStats = null,
        )

    private fun createMockPlayerStatDto(): MatchPlayerStatPlanDto =
        MatchPlayerStatPlanDto(
            homePlayerStatList = emptyList(),
            awayPlayerStatList = emptyList(),
        )

    private fun createMockPlayerContext(): MatchPlayerContext =
        MatchPlayerContext().apply {
            lineupMpDtoMap["mp_id_1"] = createMockPlayerDto("Player 1", 1L)
            lineupMpDtoMap["mp_id_2"] = createMockPlayerDto("Player 2", 2L)
        }

    private fun createMockPlayerDto(
        name: String,
        apiId: Long,
    ): MatchPlayerDto =
        MatchPlayerDto(
            matchPlayerUid = null,
            apiId = apiId,
            name = name,
            number = 10,
            position = "F",
            grid = "10:10",
            substitute = false,
            nonLineupPlayer = false,
            teamApiId = 1L,
            playerApiSportsInfo = null,
        )

    private fun createMockFixture(): FixtureApiSports {
        val mockSeason = mock<LeagueApiSportsSeason>()
        return FixtureApiSports(
            apiId = 12345L,
            referee = "Test Referee",
            timezone = "UTC",
            date = java.time.Instant.now(),
            round = "Round 1",
            status = null,
            score = null,
            venue = null,
            season = mockSeason,
            homeTeam = null,
            awayTeam = null,
        )
    }

    private fun createMockMatchTeam(name: String): ApiSportsMatchTeam =
        ApiSportsMatchTeam(
            teamApiSports = null,
            formation = "4-3-3",
            playerColor = null,
            goalkeeperColor = null,
            winner = null,
            teamStatistics = null,
            players = mutableListOf(),
        )

    private fun createMockMatchPlayer(name: String): ApiSportsMatchPlayer =
        ApiSportsMatchPlayer(
            matchPlayerUid = "mp_$name",
            playerApiSports = null,
            name = name,
            number = 10,
            position = "F",
            grid = "10:10",
            substitute = false,
            matchTeam = null,
        )

    private fun createMockMatchEvent(): ApiSportsMatchEvent =
        ApiSportsMatchEvent(
            fixtureApi = createMockFixture(),
            matchTeam = null,
            player = null,
            assist = null,
            sequence = 1,
            elapsedTime = 15,
            extraTime = null,
            eventType = "Goal",
            detail = "Normal Goal",
            comments = "Goal scored",
        )

    private fun createMockPlayerStats(playerName: String): ApiSportsMatchPlayerStatistics {
        val mockMatchPlayer = createMockMatchPlayer(playerName)
        return ApiSportsMatchPlayerStatistics(
            matchPlayer = mockMatchPlayer,
            minutesPlayed = 90,
            shirtNumber = 10,
            position = "F",
            rating = 7.5,
            isCaptain = false,
            isSubstitute = false,
            offsides = 0,
            shotsTotal = 3,
            shotsOnTarget = 2,
            goalsTotal = 1,
            goalsConceded = 0,
            assists = 1,
            saves = 0,
            passesTotal = 45,
            keyPasses = 2,
            passesAccuracy = 85,
            tacklesTotal = 2,
            blocks = 1,
            interceptions = 1,
            duelsTotal = 8,
            duelsWon = 5,
            dribblesAttempts = 3,
            dribblesSuccess = 2,
            dribblesPast = 1,
            foulsDrawn = 2,
            foulsCommitted = 1,
            yellowCards = 0,
            redCards = 0,
            penaltyWon = 0,
            penaltyCommitted = 0,
            penaltyScored = 0,
            penaltyMissed = 0,
            penaltySaved = 0,
        )
    }
}
