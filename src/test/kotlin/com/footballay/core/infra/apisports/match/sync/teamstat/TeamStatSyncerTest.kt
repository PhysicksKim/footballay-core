package com.footballay.core.infra.apisports.match.sync.teamstat

import com.footballay.core.infra.apisports.match.dto.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.sync.teamstat.TeamStatSyncer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.assertNull

class TeamStatSyncerTest {

    private lateinit var teamStatSyncer: TeamStatSyncer

    @BeforeEach
    fun setUp() {
        teamStatSyncer = TeamStatSyncer()
    }

    @Test
    fun `정상적인 팀 통계 데이터를 성공적으로 추출한다`() {
        // given
        val dto = createDtoWithNormalStatistics()

        // when
        val result = teamStatSyncer.extractTeamStats(dto)

        // then
        assertNotNull(result)
        assertNotNull(result.homeStats)
        assertNotNull(result.awayStats)
        
        // 홈팀 통계 검증
        result.homeStats!!.let { homeStats ->
            assertEquals(100L, homeStats.teamApiId)
            assertEquals(8, homeStats.shotsOnGoal)
            assertEquals(5, homeStats.shotsOffGoal)
            assertEquals(13, homeStats.totalShots)
            assertEquals(2, homeStats.blockedShots)
            assertEquals(10, homeStats.shotsInsideBox)
            assertEquals(3, homeStats.shotsOutsideBox)
            assertEquals(12, homeStats.fouls)
            assertEquals(6, homeStats.cornerKicks)
            assertEquals(3, homeStats.offsides)
            assertEquals("65%", homeStats.ballPossession)
            assertEquals(2, homeStats.yellowCards)
            assertEquals(0, homeStats.redCards)
            assertEquals(4, homeStats.goalkeeperSaves)
            assertEquals(450, homeStats.totalPasses)
            assertEquals(405, homeStats.passesAccurate)
            assertEquals("90%", homeStats.passesPercentage)
            assertEquals(1, homeStats.goalsPrevented)
            
            // xG 검증
            assertEquals(1, homeStats.xgList.size)
            assertEquals(2.45, homeStats.xgList[0].xg)
            assertEquals(67, homeStats.xgList[0].elapsed)
        }
        
        // 원정팀 통계 검증
        result.awayStats!!.let { awayStats ->
            assertEquals(200L, awayStats.teamApiId)
            assertEquals(4, awayStats.shotsOnGoal)
            assertEquals(3, awayStats.shotsOffGoal)
            assertEquals(7, awayStats.totalShots)
            assertEquals(1, awayStats.blockedShots)
            assertEquals(5, awayStats.shotsInsideBox)
            assertEquals(2, awayStats.shotsOutsideBox)
            assertEquals(8, awayStats.fouls)
            assertEquals(3, awayStats.cornerKicks)
            assertEquals(1, awayStats.offsides)
            assertEquals("35%", awayStats.ballPossession)
            assertEquals(1, awayStats.yellowCards)
            assertEquals(0, awayStats.redCards)
            assertEquals(6, awayStats.goalkeeperSaves)
            assertEquals(250, awayStats.totalPasses)
            assertEquals(200, awayStats.passesAccurate)
            assertEquals("80%", awayStats.passesPercentage)
            assertEquals(0, awayStats.goalsPrevented)
            
            // xG 검증
            assertEquals(1, awayStats.xgList.size)
            assertEquals(1.23, awayStats.xgList[0].xg)
            assertEquals(67, awayStats.xgList[0].elapsed)
        }
    }

    @Test
    fun `통계 데이터가 없는 경우 빈 DTO를 반환한다`() {
        // given
        val dto = createDtoWithEmptyStatistics()

        // when
        val result = teamStatSyncer.extractTeamStats(dto)

        // then
        assertNotNull(result)
        assertNull(result.homeStats)
        assertNull(result.awayStats)
    }

    @Test
    fun `통계 데이터가 1개인 경우 빈 DTO를 반환한다`() {
        // given
        val dto = createDtoWithHomeTeamStatisticsOnly()

        // when
        val result = teamStatSyncer.extractTeamStats(dto)

        // then
        assertNotNull(result)
        assertNull(result.homeStats)
        assertNull(result.awayStats)
    }

    @Test
    fun `홈팀 통계만 있는 경우 빈 DTO를 반환한다`() {
        // given
        val dto = createDtoWithHomeTeamStatisticsOnly()

        // when
        val result = teamStatSyncer.extractTeamStats(dto)

        // then
        assertNotNull(result)
        assertNull(result.homeStats)
        assertNull(result.awayStats)
    }

    @Test
    fun `원정팀 통계만 있는 경우 빈 DTO를 반환한다`() {
        // given
        val dto = createDtoWithAwayTeamStatisticsOnly()

        // when
        val result = teamStatSyncer.extractTeamStats(dto)

        // then
        assertNotNull(result)
        assertNull(result.homeStats)
        assertNull(result.awayStats)
    }

    @Test
    fun `팀 ID가 null인 경우 home away stats 가 null 이어야 한다`() {
        // given
        val dto = createDtoWithNullTeamId()

        // when
        val result = teamStatSyncer.extractTeamStats(dto)

        // then
        assertNotNull(result)
        assertNull(result.homeStats)
        assertNull(result.awayStats)
    }

    @Test
    fun `경기 시간이 null이고 xG가 있는 경우 xG 리스트가 비어있다`() {
        // given
        val dto = createDtoWithNullElapsedTime()

        // when
        val result = teamStatSyncer.extractTeamStats(dto)

        // then
        assertNotNull(result)
        assertNotNull(result.homeStats)
        assertTrue(result.homeStats!!.xgList.isEmpty())
    }

    @Test
    fun `xG가 null인 경우 xG 리스트가 비어있다`() {
        // given
        val dto = createDtoWithNullXG()

        // when
        val result = teamStatSyncer.extractTeamStats(dto)

        // then
        assertNotNull(result)
        assertNotNull(result.homeStats)
        assertTrue(result.homeStats!!.xgList.isEmpty())
    }

    @Test
    fun `모든 통계 필드가 null인 경우에도 정상 처리된다`() {
        // given
        val dto = createDtoWithAllNullStatistics()

        // when
        val result = teamStatSyncer.extractTeamStats(dto)

        // then
        assertNotNull(result)
        assertNotNull(result.homeStats)
        assertNotNull(result.awayStats)
        
        result.homeStats!!.let { homeStats ->
            assertEquals(100L, homeStats.teamApiId)
            assertNull(homeStats.shotsOnGoal)
            assertNull(homeStats.shotsOffGoal)
            assertNull(homeStats.totalShots)
            assertNull(homeStats.ballPossession)
            assertNull(homeStats.passesPercentage)
            assertTrue(homeStats.xgList.isEmpty())
        }
    }

    @Test
    fun `잘못된 팀 ID로 매칭되지 않는 통계는 빈 DTO를 반환한다`() {
        // given
        val dto = createDtoWithMismatchedTeamIds()

        // when
        val result = teamStatSyncer.extractTeamStats(dto)

        // then
        assertNotNull(result)
        assertNull(result.homeStats)
        assertNull(result.awayStats)
    }

    @Test
    fun `xG 값이 잘못된 형식인 경우 0으로 처리된다`() {
        // given
        val dto = createDtoWithInvalidXGFormat()

        // when
        val result = teamStatSyncer.extractTeamStats(dto)

        // then
        assertNotNull(result)
        assertNotNull(result.homeStats)
        assertEquals(1, result.homeStats!!.xgList.size)
        assertEquals(0.0, result.homeStats!!.xgList[0].xg)
        assertEquals(67, result.homeStats!!.xgList[0].elapsed)
    }

    @Test
    fun `빈 문자열 xG 값인 경우 0으로 처리된다`() {
        // given
        val dto = createDtoWithEmptyXG()

        // when
        val result = teamStatSyncer.extractTeamStats(dto)

        // then
        assertNotNull(result)
        assertNotNull(result.homeStats)
        assertEquals(1, result.homeStats!!.xgList.size)
        assertEquals(0.0, result.homeStats!!.xgList[0].xg)
        assertEquals(67, result.homeStats!!.xgList[0].elapsed)
    }

    // ========== 테스트 데이터 생성 메서드들 ==========

    private fun createDtoWithNormalStatistics(): FullMatchSyncDto {
        return FullMatchSyncDto(
            fixture = createFixtureWithElapsed(67),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = emptyList(),
            statistics = listOf(
                createHomeTeamStatistics(),
                createAwayTeamStatistics()
            ),
            players = emptyList()
        )
    }

    private fun createDtoWithEmptyStatistics(): FullMatchSyncDto {
        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = emptyList(),
            statistics = emptyList(),
            players = emptyList()
        )
    }

    private fun createDtoWithHomeTeamStatisticsOnly(): FullMatchSyncDto {
        return FullMatchSyncDto(
            fixture = createFixtureWithElapsed(67),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = emptyList(),
            statistics = listOf(createHomeTeamStatistics()),
            players = emptyList()
        )
    }

    private fun createDtoWithAwayTeamStatisticsOnly(): FullMatchSyncDto {
        return FullMatchSyncDto(
            fixture = createFixtureWithElapsed(67),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = emptyList(),
            statistics = listOf(createAwayTeamStatistics()),
            players = emptyList()
        )
    }

    private fun createDtoWithNullTeamId(): FullMatchSyncDto {
        return FullMatchSyncDto(
            fixture = createFixtureWithElapsed(67),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = emptyList(),
            statistics = listOf(
                FullMatchSyncDto.TeamStatisticsDto(
                    team = FullMatchSyncDto.TeamSimpleDto(id = null, name = "Arsenal", logo = "arsenal.png"),
                    statistics = createHomeTeamStatisticsDetail()
                ),
                createAwayTeamStatistics()
            ),
            players = emptyList()
        )
    }

    private fun createDtoWithNullElapsedTime(): FullMatchSyncDto {
        return FullMatchSyncDto(
            fixture = createFixtureWithElapsed(null),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = emptyList(),
            statistics = listOf(createHomeTeamStatistics(), createAwayTeamStatistics()),
            players = emptyList()
        )
    }

    private fun createDtoWithNullXG(): FullMatchSyncDto {
        return FullMatchSyncDto(
            fixture = createFixtureWithElapsed(67),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = emptyList(),
            statistics = listOf(
                FullMatchSyncDto.TeamStatisticsDto(
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    statistics = createHomeTeamStatisticsDetail().copy(expectedGoals = null)
                ),
                createAwayTeamStatistics()
            ),
            players = emptyList()
        )
    }

    private fun createDtoWithAllNullStatistics(): FullMatchSyncDto {
        return FullMatchSyncDto(
            fixture = createFixtureWithElapsed(67),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = emptyList(),
            statistics = listOf(
                FullMatchSyncDto.TeamStatisticsDto(
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    statistics = createAllNullStatisticsDetail()
                ),
                FullMatchSyncDto.TeamStatisticsDto(
                    team = FullMatchSyncDto.TeamSimpleDto(id = 200L, name = "Manchester City", logo = "city.png"),
                    statistics = createAllNullStatisticsDetail()
                )
            ),
            players = emptyList()
        )
    }

    private fun createDtoWithMismatchedTeamIds(): FullMatchSyncDto {
        return FullMatchSyncDto(
            fixture = createFixtureWithElapsed(67),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = emptyList(),
            statistics = listOf(
                FullMatchSyncDto.TeamStatisticsDto(
                    team = FullMatchSyncDto.TeamSimpleDto(id = 999L, name = "Unknown Team", logo = "unknown.png"),
                    statistics = createHomeTeamStatisticsDetail()
                ),
                FullMatchSyncDto.TeamStatisticsDto(
                    team = FullMatchSyncDto.TeamSimpleDto(id = 888L, name = "Another Unknown Team", logo = "unknown2.png"),
                    statistics = createAwayTeamStatisticsDetail()
                )
            ),
            players = emptyList()
        )
    }

    private fun createDtoWithInvalidXGFormat(): FullMatchSyncDto {
        return FullMatchSyncDto(
            fixture = createFixtureWithElapsed(67),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = emptyList(),
            statistics = listOf(
                FullMatchSyncDto.TeamStatisticsDto(
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    statistics = createHomeTeamStatisticsDetail().copy(expectedGoals = "invalid_xg")
                ),
                createAwayTeamStatistics()
            ),
            players = emptyList()
        )
    }

    private fun createDtoWithEmptyXG(): FullMatchSyncDto {
        return FullMatchSyncDto(
            fixture = createFixtureWithElapsed(67),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = emptyList(),
            statistics = listOf(
                FullMatchSyncDto.TeamStatisticsDto(
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    statistics = createHomeTeamStatisticsDetail().copy(expectedGoals = "")
                ),
                createAwayTeamStatistics()
            ),
            players = emptyList()
        )
    }

    // ========== 헬퍼 메서드들 ==========

    private fun createFixtureWithElapsed(elapsed: Int?): FullMatchSyncDto.FixtureDto {
        return FullMatchSyncDto.FixtureDto(
            id = 12345L,
            referee = "Michael Oliver",
            timezone = "UTC",
            date = OffsetDateTime.of(2024, 3, 15, 15, 0, 0, 0, ZoneOffset.UTC),
            timestamp = 1710513600L,
            periods = FullMatchSyncDto.FixtureDto.PeriodsDto(first = 1710513600L, second = 1710517200L),
            venue = FullMatchSyncDto.FixtureDto.VenueDto(id = 555L, name = "Emirates Stadium", city = "London"),
            status = FullMatchSyncDto.FixtureDto.StatusDto(long = "Match Finished", short = "FT", elapsed = elapsed, extra = null)
        )
    }

    private fun createNormalFixture(): FullMatchSyncDto.FixtureDto {
        return createFixtureWithElapsed(90)
    }

    private fun createNormalLeague(): FullMatchSyncDto.LeagueDto {
        return FullMatchSyncDto.LeagueDto(
            id = 39L,
            name = "Premier League",
            country = "England",
            logo = "logo.png",
            flag = "flag.png",
            season = 2024,
            round = "Regular Season - 29",
            standings = true
        )
    }

    private fun createNormalTeams(): FullMatchSyncDto.TeamsDto {
        return FullMatchSyncDto.TeamsDto(
            home = FullMatchSyncDto.TeamsDto.TeamDto(id = 100L, name = "Arsenal", logo = "arsenal.png", winner = true),
            away = FullMatchSyncDto.TeamsDto.TeamDto(id = 200L, name = "Manchester City", logo = "city.png", winner = false)
        )
    }

    private fun createNormalScore(): FullMatchSyncDto.ScoreDto {
        return FullMatchSyncDto.ScoreDto(
            halftime = FullMatchSyncDto.ScoreDto.PairDto(home = 1, away = 0),
            fulltime = FullMatchSyncDto.ScoreDto.PairDto(home = 2, away = 1),
            extratime = null,
            penalty = null
        )
    }

    private fun createHomeTeamStatistics(): FullMatchSyncDto.TeamStatisticsDto {
        return FullMatchSyncDto.TeamStatisticsDto(
            team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
            statistics = createHomeTeamStatisticsDetail()
        )
    }

    private fun createAwayTeamStatistics(): FullMatchSyncDto.TeamStatisticsDto {
        return FullMatchSyncDto.TeamStatisticsDto(
            team = FullMatchSyncDto.TeamSimpleDto(id = 200L, name = "Manchester City", logo = "city.png"),
            statistics = createAwayTeamStatisticsDetail()
        )
    }

    private fun createHomeTeamStatisticsDetail(): FullMatchSyncDto.TeamStatisticsDto.TeamStatisticsDetailDto {
        return FullMatchSyncDto.TeamStatisticsDto.TeamStatisticsDetailDto(
            shotsOnGoal = 8,
            shotsOffGoal = 5,
            totalShots = 13,
            blockedShots = 2,
            shotsInsideBox = 10,
            shotsOutsideBox = 3,
            fouls = 12,
            cornerKicks = 6,
            offsides = 3,
            ballPossession = "65%",
            yellowCards = 2,
            redCards = 0,
            goalkeeperSaves = 4,
            totalPasses = 450,
            passesAccurate = 405,
            passesPercentage = "90%",
            expectedGoals = "2.45",
            goalsPrevented = 1
        )
    }

    private fun createAwayTeamStatisticsDetail(): FullMatchSyncDto.TeamStatisticsDto.TeamStatisticsDetailDto {
        return FullMatchSyncDto.TeamStatisticsDto.TeamStatisticsDetailDto(
            shotsOnGoal = 4,
            shotsOffGoal = 3,
            totalShots = 7,
            blockedShots = 1,
            shotsInsideBox = 5,
            shotsOutsideBox = 2,
            fouls = 8,
            cornerKicks = 3,
            offsides = 1,
            ballPossession = "35%",
            yellowCards = 1,
            redCards = 0,
            goalkeeperSaves = 6,
            totalPasses = 250,
            passesAccurate = 200,
            passesPercentage = "80%",
            expectedGoals = "1.23",
            goalsPrevented = 0
        )
    }

    private fun createAllNullStatisticsDetail(): FullMatchSyncDto.TeamStatisticsDto.TeamStatisticsDetailDto {
        return FullMatchSyncDto.TeamStatisticsDto.TeamStatisticsDetailDto(
            shotsOnGoal = null,
            shotsOffGoal = null,
            totalShots = null,
            blockedShots = null,
            shotsInsideBox = null,
            shotsOutsideBox = null,
            fouls = null,
            cornerKicks = null,
            offsides = null,
            ballPossession = null,
            yellowCards = null,
            redCards = null,
            goalkeeperSaves = null,
            totalPasses = null,
            passesAccurate = null,
            passesPercentage = null,
            expectedGoals = null,
            goalsPrevented = null
        )
    }
}