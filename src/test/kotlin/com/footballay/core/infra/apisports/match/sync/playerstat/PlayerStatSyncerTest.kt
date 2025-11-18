package com.footballay.core.infra.apisports.match.sync.playerstat

import com.footballay.core.infra.apisports.match.dto.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerKeyGenerator.generateMatchPlayerKey
import com.footballay.core.infra.apisports.match.sync.dto.MatchPlayerDto
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import java.time.OffsetDateTime

class PlayerStatSyncerTest {
    private lateinit var matchPlayerStatExtractorImpl: MatchPlayerStatExtractorImpl
    private lateinit var context: MatchPlayerContext

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        matchPlayerStatExtractorImpl = MatchPlayerStatExtractorImpl()
        context = MatchPlayerContext()
    }

    @Test
    fun `빈 선수 통계 데이터 처리`() {
        // given
        val dto = createFullMatchSyncDto(players = emptyList())

        // when
        val result = matchPlayerStatExtractorImpl.extractPlayerStats(dto, context)

        // then
        assertTrue(result.homePlayerStatList.isEmpty())
        assertTrue(result.awayPlayerStatList.isEmpty())
    }

    @Test
    fun `팀 ID가 null인 경우 처리`() {
        // given
        val dto =
            createFullMatchSyncDto(
                homeTeamId = null,
                awayTeamId = 2L,
            )

        // when
        val result = matchPlayerStatExtractorImpl.extractPlayerStats(dto, context)

        // then
        assertTrue(result.homePlayerStatList.isEmpty())
        assertTrue(result.awayPlayerStatList.isEmpty())
    }

    @Test
    fun `선수 이름이 null인 경우 스킵`() {
        // given
        val homePlayerStats =
            createPlayerStatisticsDto(
                teamId = 1L,
                players =
                    listOf(
                        createPlayerDetailDto(
                            playerId = 100L,
                            playerName = null,
                            statistics = createValidStatistics(),
                        ),
                    ),
            )
        val dto = createFullMatchSyncDto(players = listOf(homePlayerStats))

        // when
        val result = matchPlayerStatExtractorImpl.extractPlayerStats(dto, context)

        // then
        assertTrue(result.homePlayerStatList.isEmpty())
    }

    @Test
    fun `선수 이름이 빈 문자열인 경우 스킵`() {
        // given
        val homePlayerStats =
            createPlayerStatisticsDto(
                teamId = 1L,
                players =
                    listOf(
                        createPlayerDetailDto(
                            playerId = 100L,
                            playerName = "",
                            statistics = createValidStatistics(),
                        ),
                    ),
            )
        val dto = createFullMatchSyncDto(players = listOf(homePlayerStats))

        // when
        val result = matchPlayerStatExtractorImpl.extractPlayerStats(dto, context)

        // then
        assertTrue(result.homePlayerStatList.isEmpty())
    }

    @Test
    fun `통계 데이터가 null인 경우 처리`() {
        // given
        val homePlayerStats =
            createPlayerStatisticsDto(
                teamId = 1L,
                players =
                    listOf(
                        createPlayerDetailDto(
                            playerId = 100L,
                            playerName = "Test Player",
                            statistics = null,
                        ),
                    ),
            )
        val dto = createFullMatchSyncDto(players = listOf(homePlayerStats))

        // when
        val result = matchPlayerStatExtractorImpl.extractPlayerStats(dto, context)

        // then
        assertEquals(1, result.homePlayerStatList.size)
        val playerStat = result.homePlayerStatList[0]
        assertEquals(100L, playerStat.playerApiId)
        assertEquals("Test Player", playerStat.name)
        assertNull(playerStat.rating)
        assertNull(playerStat.minutesPlayed)
    }

    @Test
    fun `rating 문자열 변환 테스트`() {
        // given
        val homePlayerStats =
            createPlayerStatisticsDto(
                teamId = 1L,
                players =
                    listOf(
                        createPlayerDetailDto(
                            playerId = 100L,
                            playerName = "Test Player",
                            statistics = createStatisticsWithRating("7.5"),
                        ),
                    ),
            )
        val dto = createFullMatchSyncDto(players = listOf(homePlayerStats))

        // when
        val result = matchPlayerStatExtractorImpl.extractPlayerStats(dto, context)

        // then
        assertEquals(1, result.homePlayerStatList.size)
        assertEquals(7.5, result.homePlayerStatList[0].rating)
    }

    @Test
    fun `rating이 invalid 문자열인 경우 null 처리`() {
        // given
        val homePlayerStats =
            createPlayerStatisticsDto(
                teamId = 1L,
                players =
                    listOf(
                        createPlayerDetailDto(
                            playerId = 100L,
                            playerName = "Test Player",
                            statistics = createStatisticsWithRating("invalid"),
                        ),
                    ),
            )
        val dto = createFullMatchSyncDto(players = listOf(homePlayerStats))

        // when
        val result = matchPlayerStatExtractorImpl.extractPlayerStats(dto, context)

        // then
        assertEquals(1, result.homePlayerStatList.size)
        assertNull(result.homePlayerStatList[0].rating)
    }

    @Test
    fun `passesAccuracy 문자열 변환 테스트`() {
        // given
        val homePlayerStats =
            createPlayerStatisticsDto(
                teamId = 1L,
                players =
                    listOf(
                        createPlayerDetailDto(
                            playerId = 100L,
                            playerName = "Test Player",
                            statistics = createStatisticsWithPassesAccuracy("88"),
                        ),
                    ),
            )
        val dto = createFullMatchSyncDto(players = listOf(homePlayerStats))

        // when
        val result = matchPlayerStatExtractorImpl.extractPlayerStats(dto, context)

        // then
        assertEquals(1, result.homePlayerStatList.size)
        assertEquals(88, result.homePlayerStatList[0].passesAccuracy)
    }

    @Test
    fun `라인업에 있는 선수 매칭 성공`() {
        // given
        val playerName = "Test Player"
        val playerId = 100L
        val mpKey = generateMatchPlayerKey(playerId, playerName)

        context.lineupMpDtoMap[mpKey] =
            MatchPlayerDto(
                apiId = playerId,
                name = playerName,
                number = 10,
                position = "F",
                grid = "4:1",
                substitute = false,
                nonLineupPlayer = false,
                teamApiId = 1L,
                playerApiSportsInfo = null,
            )

        val homePlayerStats =
            createPlayerStatisticsDto(
                teamId = 1L,
                players =
                    listOf(
                        createPlayerDetailDto(
                            playerId = playerId,
                            playerName = playerName,
                            statistics = createValidStatistics(),
                        ),
                    ),
            )
        val dto = createFullMatchSyncDto(players = listOf(homePlayerStats))

        // when
        val result = matchPlayerStatExtractorImpl.extractPlayerStats(dto, context)

        // then
        assertEquals(1, result.homePlayerStatList.size)
        assertEquals(playerId, result.homePlayerStatList[0].playerApiId)
        assertEquals(playerName, result.homePlayerStatList[0].name)
        assertEquals(context.statMpDtoMap.size, 0)
        assertEquals(context.lineupMpDtoMap.size, 1)
        assertEquals(context.eventMpDtoMap.size, 0)
    }

    @Test
    fun `라인업에 없는 선수는 statMpDtoMap에 저장`() {
        // given
        val playerName = "Unknown Player"
        val playerId = 999L
        val mpKey = generateMatchPlayerKey(playerId, playerName)

        val homePlayerStats =
            createPlayerStatisticsDto(
                teamId = 1L,
                players =
                    listOf(
                        createPlayerDetailDto(
                            playerId = playerId,
                            playerName = playerName,
                            statistics = createValidStatistics(),
                        ),
                    ),
            )
        val dto = createFullMatchSyncDto(players = listOf(homePlayerStats))

        // when
        val result = matchPlayerStatExtractorImpl.extractPlayerStats(dto, context)

        // then
        assertEquals(1, result.homePlayerStatList.size)
        assertTrue(context.statMpDtoMap.containsKey(mpKey))
        val createdPlayer = context.statMpDtoMap[mpKey]
        assertEquals(playerId, createdPlayer?.apiId)
        assertEquals(playerName, createdPlayer?.name)
        assertTrue(createdPlayer?.nonLineupPlayer == true)
    }

    @Test
    fun `id가 null인 선수 처리`() {
        // given
        val playerName = "Null ID Player"
        val mpKey = generateMatchPlayerKey(null, playerName)

        val homePlayerStats =
            createPlayerStatisticsDto(
                teamId = 1L,
                players =
                    listOf(
                        createPlayerDetailDto(
                            playerId = null,
                            playerName = playerName,
                            statistics = createValidStatistics(),
                        ),
                    ),
            )
        val dto = createFullMatchSyncDto(players = listOf(homePlayerStats))

        // when
        val result = matchPlayerStatExtractorImpl.extractPlayerStats(dto, context)

        // then
        assertEquals(1, result.homePlayerStatList.size)
        assertTrue(context.statMpDtoMap.containsKey(mpKey))
        val createdPlayer = context.statMpDtoMap[mpKey]
        assertNull(createdPlayer?.apiId)
        assertEquals(playerName, createdPlayer?.name)
    }

    @Test
    fun `팀 ID가 null인 선수 통계 처리`() {
        // given
        val homePlayerStats =
            createPlayerStatisticsDto(
                teamId = null,
                players =
                    listOf(
                        createPlayerDetailDto(
                            playerId = 100L,
                            playerName = "Test Player",
                            statistics = createValidStatistics(),
                        ),
                    ),
            )
        val dto = createFullMatchSyncDto(players = listOf(homePlayerStats))

        // when
        val result = matchPlayerStatExtractorImpl.extractPlayerStats(dto, context)

        // then
        assertTrue(result.homePlayerStatList.isEmpty())
    }

    @Test
    fun `여러 선수 통계 처리`() {
        // given
        val homePlayerStats =
            createPlayerStatisticsDto(
                teamId = 1L,
                players =
                    listOf(
                        createPlayerDetailDto(
                            playerId = 100L,
                            playerName = "Player 1",
                            statistics = createValidStatistics(),
                        ),
                        createPlayerDetailDto(
                            playerId = 101L,
                            playerName = "Player 2",
                            statistics = createValidStatistics(),
                        ),
                    ),
            )
        val dto = createFullMatchSyncDto(players = listOf(homePlayerStats))

        // when
        val result = matchPlayerStatExtractorImpl.extractPlayerStats(dto, context)

        // then
        assertEquals(2, result.homePlayerStatList.size)
        assertEquals("Player 1", result.homePlayerStatList[0].name)
        assertEquals("Player 2", result.homePlayerStatList[1].name)
    }

    @Test
    fun `홈팀과 원정팀 선수 분리 처리`() {
        // given
        val homePlayerStats =
            createPlayerStatisticsDto(
                teamId = 1L,
                players =
                    listOf(
                        createPlayerDetailDto(
                            playerId = 100L,
                            playerName = "Home Player",
                            statistics = createValidStatistics(),
                        ),
                    ),
            )
        val awayPlayerStats =
            createPlayerStatisticsDto(
                teamId = 2L,
                players =
                    listOf(
                        createPlayerDetailDto(
                            playerId = 200L,
                            playerName = "Away Player",
                            statistics = createValidStatistics(),
                        ),
                    ),
            )
        val dto = createFullMatchSyncDto(players = listOf(homePlayerStats, awayPlayerStats))

        // when
        val result = matchPlayerStatExtractorImpl.extractPlayerStats(dto, context)

        // then
        assertEquals(1, result.homePlayerStatList.size)
        assertEquals(1, result.awayPlayerStatList.size)
        assertEquals("Home Player", result.homePlayerStatList[0].name)
        assertEquals("Away Player", result.awayPlayerStatList[0].name)
    }

    // Helper methods for creating test data
    private fun createFullMatchSyncDto(
        homeTeamId: Long? = 1L,
        awayTeamId: Long? = 2L,
        players: List<FullMatchSyncDto.PlayerStatisticsDto> = emptyList(),
    ): FullMatchSyncDto =
        FullMatchSyncDto(
            fixture =
                FullMatchSyncDto.FixtureDto(
                    id = 1L,
                    referee = "Test Referee",
                    timezone = "UTC",
                    date = OffsetDateTime.now(),
                    timestamp = 1234567890L,
                    periods = FullMatchSyncDto.FixtureDto.PeriodsDto(1234567890L, 1234567890L),
                    venue = FullMatchSyncDto.FixtureDto.VenueDto(1L, "Test Venue", "Test City"),
                    status = FullMatchSyncDto.FixtureDto.StatusDto("Finished", "FT", 90, 0),
                ),
            league =
                FullMatchSyncDto.LeagueDto(
                    id = 1L,
                    name = "Test League",
                    country = "Test Country",
                    logo = "test-logo.png",
                    flag = "test-flag.png",
                    season = 2024,
                    round = "Regular Season",
                    standings = true,
                ),
            teams =
                FullMatchSyncDto.TeamsDto(
                    home = FullMatchSyncDto.TeamsDto.TeamDto(homeTeamId, "Home Team", "home-logo.png", true),
                    away = FullMatchSyncDto.TeamsDto.TeamDto(awayTeamId, "Away Team", "away-logo.png", false),
                ),
            goals = FullMatchSyncDto.GoalsDto(2, 1),
            score =
                FullMatchSyncDto.ScoreDto(
                    halftime = FullMatchSyncDto.ScoreDto.PairDto(1, 0),
                    fulltime = FullMatchSyncDto.ScoreDto.PairDto(2, 1),
                    extratime = null,
                    penalty = null,
                ),
            events = emptyList(),
            lineups = emptyList(),
            statistics = emptyList(),
            players = players,
        )

    private fun createPlayerStatisticsDto(
        teamId: Long?,
        players: List<FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto>,
    ): FullMatchSyncDto.PlayerStatisticsDto =
        FullMatchSyncDto.PlayerStatisticsDto(
            team = FullMatchSyncDto.TeamSimpleDto(teamId, "Test Team", "test-logo.png"),
            players = players,
        )

    private fun createPlayerDetailDto(
        playerId: Long?,
        playerName: String?,
        statistics: FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto?,
    ): FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto =
        FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto(
            player =
                FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.PlayerDetailInfoDto(
                    id = playerId,
                    name = playerName,
                    photo = "test-photo.png",
                ),
            statistics = if (statistics != null) listOf(statistics) else emptyList(),
        )

    private fun createValidStatistics(): FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto =
        FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto(
            games =
                FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto.GameStatsDto(
                    minutes = 90,
                    number = 10,
                    position = "F",
                    rating = "7.5",
                    captain = false,
                    substitute = false,
                ),
            offsides = 2,
            shots =
                FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto
                    .ShotStatsDto(5, 3),
            goals =
                FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto
                    .GoalStatsDto(1, 0, 1, 0),
            passes =
                FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto
                    .PassStatsDto(50, 3, "85"),
            tackles =
                FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto
                    .TackleStatsDto(2, 1, 1),
            duels =
                FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto
                    .DuelStatsDto(10, 6),
            dribbles =
                FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto
                    .DribbleStatsDto(3, 2, 1),
            fouls =
                FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto
                    .FoulStatsDto(2, 1),
            cards =
                FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto
                    .CardStatsDto(0, 0),
            penalty =
                FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto
                    .PenaltyStatsDto(0, 0, 0, 0, 0),
        )

    private fun createStatisticsWithRating(
        rating: String?,
    ): FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto =
        FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto(
            games =
                FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto.GameStatsDto(
                    minutes = 90,
                    number = 10,
                    position = "F",
                    rating = rating,
                    captain = false,
                    substitute = false,
                ),
            offsides = null,
            shots =
                FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto
                    .ShotStatsDto(null, null),
            goals =
                FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto
                    .GoalStatsDto(null, null, null, null),
            passes =
                FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto
                    .PassStatsDto(null, null, null),
            tackles = null,
            duels = null,
            dribbles = null,
            fouls = null,
            cards = null,
            penalty = null,
        )

    private fun createStatisticsWithPassesAccuracy(
        accuracy: String?,
    ): FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto =
        FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto(
            games =
                FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto.GameStatsDto(
                    minutes = 90,
                    number = 10,
                    position = "F",
                    rating = "7.5",
                    captain = false,
                    substitute = false,
                ),
            offsides = null,
            shots =
                FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto
                    .ShotStatsDto(null, null),
            goals =
                FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto
                    .GoalStatsDto(null, null, null, null),
            passes =
                FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto
                    .PassStatsDto(50, 3, accuracy),
            tackles = null,
            duels = null,
            dribbles = null,
            fouls = null,
            cards = null,
            penalty = null,
        )
}
