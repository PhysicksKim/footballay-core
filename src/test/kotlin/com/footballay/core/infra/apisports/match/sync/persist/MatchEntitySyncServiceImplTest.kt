package com.footballay.core.infra.apisports.match.sync.persist

import com.footballay.core.infra.apisports.match.sync.dto.MatchEventDto
import com.footballay.core.infra.apisports.match.sync.dto.PlayerStatSyncDto
import com.footballay.core.infra.apisports.match.sync.dto.TeamStatSyncDto
import com.footballay.core.infra.apisports.match.sync.context.MatchEntityBundle
import com.footballay.core.infra.apisports.match.sync.dto.LineupSyncDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchPlayerDto
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.sync.dto.FixtureApiSportsDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchEventSyncDto
import com.footballay.core.infra.apisports.match.sync.loader.MatchDataLoader
import com.footballay.core.infra.apisports.match.sync.persist.MatchEntitySyncServiceImpl
import com.footballay.core.infra.apisports.match.sync.persist.base.BaseMatchEntitySyncer
import com.footballay.core.infra.apisports.match.sync.persist.base.BaseMatchSyncResult
import com.footballay.core.infra.apisports.match.sync.persist.player.manager.MatchPlayerManager
import com.footballay.core.infra.apisports.match.sync.persist.player.manager.MatchPlayerProcessResult
import com.footballay.core.infra.apisports.match.sync.persist.event.manager.MatchEventManager
import com.footballay.core.infra.apisports.match.sync.persist.event.manager.MatchEventProcessResult
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchEvent
import com.footballay.core.infra.util.UidGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.mockito.kotlin.*
import java.time.OffsetDateTime
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsSeason

class MatchEntitySyncServiceImplTest {

    private lateinit var matchEntitySyncService: MatchEntitySyncServiceImpl
    private lateinit var matchDataLoader: MatchDataLoader
    private lateinit var baseMatchEntitySyncer: BaseMatchEntitySyncer
    private lateinit var matchPlayerManager: MatchPlayerManager
    private lateinit var matchEventManager: MatchEventManager

    @BeforeEach
    fun setUp() {
        matchDataLoader = mock()
        baseMatchEntitySyncer = mock()
        matchPlayerManager = mock()
        matchEventManager = mock()
        
        matchEntitySyncService = MatchEntitySyncServiceImpl(
            matchDataLoader,
            baseMatchEntitySyncer,
            matchPlayerManager,
            matchEventManager
        )
    }

    @Test
    @DisplayName("모든 Phase가 성공적으로 완료되어 MatchPlayer와 MatchTeam 엔티티가 정상적으로 동기화됩니다")
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
        val savedPlayers = listOf(
            createMockMatchPlayer("Player 1"),
            createMockMatchPlayer("Player 2")
        )
        
        // Mock 설정
        whenever(matchDataLoader.loadContext(eq(fixtureApiId), any(), any())).then { }
        whenever(baseMatchEntitySyncer.syncBaseEntities(
            eq(fixtureApiId), any(), any()
        )).thenReturn(BaseMatchSyncResult.success(
            fixture = createMockFixture(),
            homeMatchTeam = createMockMatchTeam("Home Team"),
            awayMatchTeam = createMockMatchTeam("Away Team")
        ))
        
        whenever(matchPlayerManager.processMatchPlayers(any(), any(), any())).thenReturn(
            MatchPlayerProcessResult(
                totalPlayers = 2,
                createdCount = 2,
                updatedCount = 0,
                deletedCount = 0,
                savedPlayers = savedPlayers
            )
        )
        
        whenever(matchEventManager.processMatchEvents(any(), any())).thenReturn(
            MatchEventProcessResult(
                totalEvents = 1,
                createdCount = 1,
                updatedCount = 0,
                deletedCount = 0,
                savedEvents = listOf(createMockMatchEvent())
            )
        )

        // when
        val result = matchEntitySyncService.syncMatchEntities(
            fixtureApiId, baseDto, lineupDto, eventDto, teamStatDto, playerStatDto, playerContext
        )

        // then
        assertThat(result.success).isTrue()
        assertThat(result.createdCount).isEqualTo(3) // 2 players + 1 event
        assertThat(result.updatedCount).isEqualTo(0)
        assertThat(result.deletedCount).isEqualTo(0)
        
        // Phase 3 & 4 검증
        verify(matchPlayerManager).processMatchPlayers(any(), any(), any())
        verify(matchEventManager).processMatchEvents(any(), any())
        verify(matchDataLoader).loadContext(eq(fixtureApiId), any(), any())
        verify(baseMatchEntitySyncer).syncBaseEntities(eq(fixtureApiId), any(), any())
    }

    @Test
    @DisplayName("Phase 3(MatchPlayer 처리)에서 예외가 발생하면 전체 동기화가 실패하고 에러 메시지를 반환합니다")
    fun testPhase3ErrorHandling() {
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
        whenever(baseMatchEntitySyncer.syncBaseEntities(
            eq(fixtureApiId), any(), any()
        )).thenReturn(BaseMatchSyncResult.success(
            fixture = createMockFixture(),
            homeMatchTeam = createMockMatchTeam("Home Team"),
            awayMatchTeam = createMockMatchTeam("Away Team")
        ))
        whenever(matchPlayerManager.processMatchPlayers(any(), any(), any())).thenThrow(
            RuntimeException("MatchPlayer processing failed")
        )

        // when
        val result = matchEntitySyncService.syncMatchEntities(
            fixtureApiId, baseDto, lineupDto, eventDto, teamStatDto, playerStatDto, playerContext
        )

        // then
        assertThat(result.success).isFalse()
        assertThat(result.errorMessage).contains("Entity sync failed")
        assertThat(result.errorMessage).contains("MatchPlayer processing failed")
    }

    @Test
    @DisplayName("Phase 2(Base 엔티티 동기화)가 실패하면 Phase 3가 실행되지 않고 즉시 실패 결과를 반환합니다")
    fun testPhase2FailurePreventsPhase3() {
        // given
        val fixtureApiId = 12345L
        val baseDto = createMockFixtureDto()
        val lineupDto = createMockLineupDto()
        val eventDto = createMockEventDto()
        val teamStatDto = createMockTeamStatDto()
        val playerStatDto = createMockPlayerStatDto()
        val playerContext = createMockPlayerContext()
        
        // Mock 설정 - Phase 2 실패
        whenever(matchDataLoader.loadContext(eq(fixtureApiId), any(), any())).then { }
        whenever(baseMatchEntitySyncer.syncBaseEntities(
            eq(fixtureApiId), any(), any()
        )).thenReturn(BaseMatchSyncResult.failure("Base entity sync failed"))

        // when
        val result = matchEntitySyncService.syncMatchEntities(
            fixtureApiId, baseDto, lineupDto, eventDto, teamStatDto, playerStatDto, playerContext
        )

        // then
        assertThat(result.success).isFalse()
        assertThat(result.errorMessage).contains("Base entity sync failed")
        
        // Phase 3가 실행되지 않았는지 확인
        verify(matchPlayerManager, never()).processMatchPlayers(any(), any(), any())
    }

    // 헬퍼 메서드들
    private fun createMockFixtureDto(): FixtureApiSportsDto {
        return FixtureApiSportsDto(
            apiId = 12345L,
            referee = "Test Referee",
            timezone = "UTC",
            date = OffsetDateTime.now(),
            timestamp = 1704067200L,
            round = "Round 1",
            status = FixtureApiSportsDto.StatusDto(
                longStatus = "Match Finished",
                shortStatus = "FT",
                elapsed = 90,
                extra = null
            ),
            score = FixtureApiSportsDto.ScoreDto(
                totalHome = 2,
                totalAway = 1,
                halftimeHome = 1,
                halftimeAway = 0,
                fulltimeHome = 2,
                fulltimeAway = 1,
                extratimeHome = null,
                extratimeAway = null,
                penaltyHome = null,
                penaltyAway = null
            ),
            venue = FixtureApiSportsDto.VenueDto(
                apiId = 1L,
                name = "Stadium",
                city = "City"
            ),
            seasonYear = 2024,
            homeTeam = FixtureApiSportsDto.BaseTeamDto(
                apiId = 1L,
                name = "Home Team",
                logo = "home_logo.png",
                winner = true
            ),
            awayTeam = FixtureApiSportsDto.BaseTeamDto(
                apiId = 2L,
                name = "Away Team",
                logo = "away_logo.png",
                winner = false
            )
        )
    }

    private fun createMockLineupDto(): LineupSyncDto {
        return LineupSyncDto(
            home = LineupSyncDto.Lineup(
                teamApiId = 1L,
                teamName = "Home Team",
                teamLogo = "home_logo.png",
                playerColor = LineupSyncDto.Color("red", "white", "red"),
                goalkeeperColor = LineupSyncDto.Color("blue", "white", "blue"),
                formation = "4-3-3",
                startMpKeys = listOf("mp_id_1", "mp_id_2"),
                subMpKeys = listOf("mp_id_3", "mp_id_4")
            ),
            away = LineupSyncDto.Lineup(
                teamApiId = 2L,
                teamName = "Away Team",
                teamLogo = "away_logo.png",
                playerColor = LineupSyncDto.Color("green", "white", "green"),
                goalkeeperColor = LineupSyncDto.Color("yellow", "black", "yellow"),
                formation = "4-4-2",
                startMpKeys = listOf("mp_id_5", "mp_id_6"),
                subMpKeys = listOf("mp_id_7", "mp_id_8")
            )
        )
    }

    private fun createMockEventDto(): MatchEventSyncDto {
        return MatchEventSyncDto(
            events = listOf(
                MatchEventDto(
                    sequence = 1,
                    elapsedTime = 15,
                    extraTime = null,
                    eventType = "Goal",
                    detail = "Normal Goal",
                    comments = "Goal scored",
                    teamApiId = 1L,
                    playerMpKey = "mp_id_1",
                    assistMpKey = "mp_id_2"
                )
            )
        )
    }

    private fun createMockTeamStatDto(): TeamStatSyncDto {
        return TeamStatSyncDto(
            homeStats = null,
            awayStats = null
        )
    }

    private fun createMockPlayerStatDto(): PlayerStatSyncDto {
        return PlayerStatSyncDto(
            homePlayerStatList = emptyList(),
            awayPlayerStatList = emptyList()
        )
    }

    private fun createMockPlayerContext(): MatchPlayerContext {
        return MatchPlayerContext().apply {
            lineupMpDtoMap["mp_id_1"] = createMockPlayerDto("Player 1", 1L)
            lineupMpDtoMap["mp_id_2"] = createMockPlayerDto("Player 2", 2L)
        }
    }

    private fun createMockPlayerDto(name: String, apiId: Long): MatchPlayerDto {
        return MatchPlayerDto(
            matchPlayerUid = null,
            apiId = apiId,
            name = name,
            number = 10,
            position = "F",
            grid = "10:10",
            substitute = false,
            nonLineupPlayer = false,
            teamApiId = 1L,
            playerApiSportsInfo = null
        )
    }

    private fun createMockFixture(): FixtureApiSports {
        val mockSeason = mock<LeagueApiSportsSeason>()
        return FixtureApiSports(
            apiId = 12345L,
            referee = "Test Referee",
            timezone = "UTC",
            date = OffsetDateTime.now(),
            timestamp = 1704067200L,
            round = "Round 1",
            status = null,
            score = null,
            venue = null,
            season = mockSeason,
            homeTeam = null,
            awayTeam = null
        )
    }

    private fun createMockMatchTeam(name: String): ApiSportsMatchTeam {
        return ApiSportsMatchTeam(
            teamApiSports = null,
            formation = "4-3-3",
            playerColor = null,
            goalkeeperColor = null,
            winner = null,
            teamStatistics = null,
            players = mutableListOf()
        )
    }

    private fun createMockMatchPlayer(name: String): ApiSportsMatchPlayer {
        return ApiSportsMatchPlayer(
            matchPlayerUid = "mp_$name",
            playerApiSports = null,
            name = name,
            number = 10,
            position = "F",
            grid = "10:10",
            substitute = false,
            matchTeam = null
        )
    }

    private fun createMockMatchEvent(): ApiSportsMatchEvent {
        return ApiSportsMatchEvent(
            fixtureApi = createMockFixture(),
            matchTeam = null,
            player = null,
            assist = null,
            sequence = 1,
            elapsedTime = 15,
            extraTime = null,
            eventType = "Goal",
            detail = "Normal Goal",
            comments = "Goal scored"
        )
    }
} 