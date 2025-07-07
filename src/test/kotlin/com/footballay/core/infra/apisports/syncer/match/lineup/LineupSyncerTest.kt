package com.footballay.core.infra.apisports.syncer.match.lineup

import com.footballay.core.infra.apisports.live.FullMatchSyncDto
import com.footballay.core.infra.apisports.syncer.match.context.MatchPlayerContext
import com.footballay.core.logger
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

class LineupSyncerTest {

    val log = logger()

    private lateinit var lineupSyncer: LineupSyncer
    private lateinit var context: MatchPlayerContext

    @BeforeEach
    fun setUp() {
        lineupSyncer = LineupSyncer()
        context = MatchPlayerContext()
    }

    @Test
    @DisplayName("정상적인 라인업 데이터를 처리할 수 있다")
    fun `should process normal lineup data successfully`() {
        // given
        val dto = createNormalLineupDto()

        // when
        val result = lineupSyncer.syncLineup(dto, context)

        // then
        assertFalse(result.isEmpty())
        assertNotNull(result.home)
        assertNotNull(result.away)
        
        // 홈팀 검증
        assertEquals(100L, result.home!!.teamApiId)
        assertEquals("Arsenal", result.home!!.teamName)
        assertEquals("4-3-3", result.home!!.formation)
        assertEquals(11, result.home!!.startMpKeys.size)
        assertEquals(7, result.home!!.subMpKeys.size)
        
        // 어웨이팀 검증
        assertEquals(200L, result.away!!.teamApiId)
        assertEquals("Manchester City", result.away!!.teamName)
        assertEquals("4-2-3-1", result.away!!.formation)
        assertEquals(11, result.away!!.startMpKeys.size)
        assertEquals(7, result.away!!.subMpKeys.size)

        // Context에 선수들이 추가되었는지 확인
        assertEquals(36, context.lineupMpDtoMap.size) // 11+7+11+7 = 36
    }

    @Test
    @DisplayName("팀 ID가 null인 경우 빈 결과를 반환한다")
    fun `should return empty result when team IDs are null`() {
        // given
        val dto = createDtoWithNullTeamIds()

        // when
        val result = lineupSyncer.syncLineup(dto, context)

        // then
        assertTrue(result.isEmpty())
        assertEquals(0, context.lineupMpDtoMap.size)
    }

    @Test
    @DisplayName("라인업 데이터가 비어있는 경우 빈 결과를 반환한다")
    fun `should return empty result when lineup data is empty`() {
        // given
        val dto = createDtoWithEmptyLineups()

        // when
        val result = lineupSyncer.syncLineup(dto, context)

        // then
        assertTrue(result.isEmpty())
        assertEquals(0, context.lineupMpDtoMap.size)
    }

    @Test
    @DisplayName("라인업에서 팀을 찾을 수 없는 경우 빈 결과를 반환한다")
    fun `should return empty result when teams cannot be found in lineups`() {
        // given
        val dto = createDtoWithMismatchedTeams()

        // when
        val result = lineupSyncer.syncLineup(dto, context)

        // then
        assertTrue(result.isEmpty())
        assertEquals(0, context.lineupMpDtoMap.size)
    }

    @Test
    @DisplayName("선수 이름이 null인 경우 해당 선수를 제외한다")
    fun `should exclude players with null names`() {
        // given
        val dto = createDtoWithNullPlayerNames()

        // when
        val result = lineupSyncer.syncLineup(dto, context)

        // then
        assertFalse(result.isEmpty())
        // null 이름 선수는 제외되므로 11명보다 적을 수 있음
        assertTrue(result.home!!.startMpKeys.size < 11)
        assertTrue(result.away!!.startMpKeys.size < 11)
    }

    @Test
    @DisplayName("선수 이름이 빈 문자열인 경우 해당 선수를 제외한다")
    fun `should exclude players with empty names`() {
        // given
        val dto = createDtoWithEmptyPlayerNames()

        // when
        val result = lineupSyncer.syncLineup(dto, context)

        // then
        assertFalse(result.isEmpty())
        // 빈 이름 선수는 제외되므로 11명보다 적을 수 있음
        assertTrue(result.home!!.startMpKeys.size < 11)
        assertTrue(result.away!!.startMpKeys.size < 11)
    }

    @Test
    @DisplayName("선수 ID가 null인 경우 이름 기반 키를 생성한다")
    fun `should generate name-based keys for players with null IDs`() {
        // given
        val dto = createDtoWithNullPlayerIds()

        // when
        val result = lineupSyncer.syncLineup(dto, context)

        // then
        assertFalse(result.isEmpty())
        
        // null ID 선수들의 키가 이름 기반으로 생성되었는지 확인
        val nullIdPlayerKeys = context.lineupMpDtoMap.keys.filter { key ->
            key.startsWith("mp_name_")
        }
        assertTrue(nullIdPlayerKeys.isNotEmpty())
        
        // ID가 있는 선수들의 키가 ID 기반으로 생성되었는지 확인
        val idBasedPlayerKeys = context.lineupMpDtoMap.keys.filter { key ->
            key.startsWith("mp_id_")
        }
        assertTrue(idBasedPlayerKeys.isNotEmpty())
    }

    @Test
    @DisplayName("선수 포지션이 null인 경우 기본값을 설정한다")
    fun `should set default position when player position is null`() {
        // given
        val dto = createDtoWithNullPlayerPositions()

        // when
        val result = lineupSyncer.syncLineup(dto, context)

        // then
        assertFalse(result.isEmpty())
        
        // null 포지션 선수들이 "Unknown"으로 설정되었는지 확인
        val unknownPositionPlayers = context.lineupMpDtoMap.values.filter { dto ->
            dto.position == "Unknown"
        }
        assertTrue(unknownPositionPlayers.isNotEmpty())
    }

    @Test
    @DisplayName("라인업 선수 수가 11명이 아닌 경우에도 정상 처리한다")
    fun `should handle lineups with abnormal player counts`() {
        // given
        val dto = createDtoWithAbnormalPlayerCounts()

        // when
        val result = lineupSyncer.syncLineup(dto, context)

        // then
        assertFalse(result.isEmpty())
        // 11명이 아니어도 정상 처리되어야 함
        assertTrue(result.home!!.startMpKeys.size > 0)
        assertTrue(result.away!!.startMpKeys.size > 0)
    }

    @Test
    @DisplayName("모든 선수 정보가 누락된 경우 빈 결과를 반환한다")
    fun `should return empty result when all player information is missing`() {
        // given
        val dto = createDtoWithAllMissingPlayerInfo()

        // when
        val result = lineupSyncer.syncLineup(dto, context)

        // then
        assertTrue(result.home!!.startMpKeys.isEmpty())
        assertTrue(result.home!!.subMpKeys.isEmpty())
        assertTrue(result.away!!.startMpKeys.isEmpty())
        assertTrue(result.away!!.subMpKeys.isEmpty())
        assertEquals(0, context.lineupMpDtoMap.size)
    }

    @Test
    @DisplayName("중복된 선수 이름이 있는 경우 모두 처리한다")
    fun `should handle duplicate player names`() {
        // given
        val dto = createDtoWithDuplicatePlayerNames()

        // when
        val result = lineupSyncer.syncLineup(dto, context)

        // then
        assertFalse(result.isEmpty())
        // 중복 이름도 모두 처리되어야 함
        val duplicateNamePlayers = context.lineupMpDtoMap.values.filter { dto ->
            dto.name == "John Doe"
        }
        assertTrue(duplicateNamePlayers.size > 1)
    }

    // ========== 테스트 데이터 생성 메서드들 ==========

    private fun createNormalLineupDto(): FullMatchSyncDto {
        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = createNormalLineups(),
            statistics = emptyList(),
            players = emptyList()
        )
    }

    private fun createDtoWithNullTeamIds(): FullMatchSyncDto {
        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = FullMatchSyncDto.TeamsDto(
                home = FullMatchSyncDto.TeamsDto.TeamDto(id = null, name = "Arsenal", logo = "logo.png", winner = null),
                away = FullMatchSyncDto.TeamsDto.TeamDto(id = null, name = "Manchester City", logo = "logo.png", winner = null)
            ),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = emptyList(),
            statistics = emptyList(),
            players = emptyList()
        )
    }

    private fun createDtoWithEmptyLineups(): FullMatchSyncDto {
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

    private fun createDtoWithMismatchedTeams(): FullMatchSyncDto {
        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = listOf(
                createLineupWithTeamId(999L, "Unknown Team", "4-4-2")
            ),
            statistics = emptyList(),
            players = emptyList()
        )
    }

    private fun createDtoWithNullPlayerNames(): FullMatchSyncDto {
        val lineups = listOf(
            createLineupWithNullPlayerNames(100L, "Arsenal", "4-3-3"),
            createLineupWithNullPlayerNames(200L, "Manchester City", "4-2-3-1")
        )
        
        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = lineups,
            statistics = emptyList(),
            players = emptyList()
        )
    }

    private fun createDtoWithEmptyPlayerNames(): FullMatchSyncDto {
        val lineups = listOf(
            createLineupWithEmptyPlayerNames(100L, "Arsenal", "4-3-3"),
            createLineupWithEmptyPlayerNames(200L, "Manchester City", "4-2-3-1")
        )
        
        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = lineups,
            statistics = emptyList(),
            players = emptyList()
        )
    }

    private fun createDtoWithNullPlayerIds(): FullMatchSyncDto {
        val lineups = listOf(
            createLineupWithNullPlayerIds(100L, "Arsenal", "4-3-3"),
            createLineupWithNullPlayerIds(200L, "Manchester City", "4-2-3-1")
        )
        
        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = lineups,
            statistics = emptyList(),
            players = emptyList()
        )
    }

    private fun createDtoWithNullPlayerPositions(): FullMatchSyncDto {
        val lineups = listOf(
            createLineupWithNullPlayerPositions(100L, "Arsenal", "4-3-3"),
            createLineupWithNullPlayerPositions(200L, "Manchester City", "4-2-3-1")
        )
        
        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = lineups,
            statistics = emptyList(),
            players = emptyList()
        )
    }

    private fun createDtoWithAbnormalPlayerCounts(): FullMatchSyncDto {
        val lineups = listOf(
            createLineupWithAbnormalPlayerCounts(100L, "Arsenal", "4-3-3"),
            createLineupWithAbnormalPlayerCounts(200L, "Manchester City", "4-2-3-1")
        )
        
        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = lineups,
            statistics = emptyList(),
            players = emptyList()
        )
    }

    private fun createDtoWithAllMissingPlayerInfo(): FullMatchSyncDto {
        val lineups = listOf(
            createLineupWithAllMissingPlayerInfo(100L, "Arsenal", "4-3-3"),
            createLineupWithAllMissingPlayerInfo(200L, "Manchester City", "4-2-3-1")
        )
        
        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = lineups,
            statistics = emptyList(),
            players = emptyList()
        )
    }

    private fun createDtoWithDuplicatePlayerNames(): FullMatchSyncDto {
        val lineups = listOf(
            createLineupWithDuplicatePlayerNames(100L, "Arsenal", "4-3-3"),
            createLineupWithDuplicatePlayerNames(200L, "Manchester City", "4-2-3-1")
        )
        
        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = lineups,
            statistics = emptyList(),
            players = emptyList()
        )
    }

    // ========== 헬퍼 메서드들 ==========

    private fun createNormalFixture(): FullMatchSyncDto.FixtureDto {
        return FullMatchSyncDto.FixtureDto(
            id = 12345L,
            referee = "Michael Oliver",
            timezone = "UTC",
            date = OffsetDateTime.of(2024, 3, 15, 15, 0, 0, 0, ZoneOffset.UTC),
            timestamp = 1710513600L,
            periods = FullMatchSyncDto.FixtureDto.PeriodsDto(first = 1710513600L, second = 1710517200L),
            venue = FullMatchSyncDto.FixtureDto.VenueDto(id = 555L, name = "Emirates Stadium", city = "London"),
            status = FullMatchSyncDto.FixtureDto.StatusDto(long = "Not Started", short = "NS", elapsed = null, extra = null)
        )
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
            home = FullMatchSyncDto.TeamsDto.TeamDto(id = 100L, name = "Arsenal", logo = "arsenal.png", winner = null),
            away = FullMatchSyncDto.TeamsDto.TeamDto(id = 200L, name = "Manchester City", logo = "city.png", winner = null)
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

    private fun createNormalLineups(): List<FullMatchSyncDto.LineupDto> {
        return listOf(
            createLineupWithTeamId(100L, "Arsenal", "4-3-3"),
            createLineupWithTeamId(200L, "Manchester City", "4-2-3-1")
        )
    }

    private fun createLineupWithTeamId(teamId: Long, teamName: String, formation: String): FullMatchSyncDto.LineupDto {
        // 팀별 고유한 선수 ID 생성: teamId * 100 + index
        val startXI = (1..11).map { index ->
            FullMatchSyncDto.LineupDto.LineupPlayerDto(
                FullMatchSyncDto.LineupDto.LineupPlayerDto.LineupPlayerDetailDto(
                    id = teamId * 100 + index, // 팀별 고유 ID: 100팀은 10001~10011, 200팀은 20001~20011
                    name = "Player $index",
                    number = index,
                    pos = if (index == 1) "G" else if (index <= 4) "D" else if (index <= 8) "M" else "F",
                    grid = "${index}:${index}"
                )
            )
        }

        val substitutes = (12..18).map { index ->
            FullMatchSyncDto.LineupDto.LineupPlayerDto(
                FullMatchSyncDto.LineupDto.LineupPlayerDto.LineupPlayerDetailDto(
                    id = teamId * 100 + 11 + index, // 후보 선수: 100팀은 10023~10029, 200팀은 20023~20029
                    name = "Sub Player $index",
                    number = index,
                    pos = if (index == 12) "G" else if (index <= 14) "D" else if (index <= 16) "M" else "F",
                    grid = null
                )
            )
        }

        return FullMatchSyncDto.LineupDto(
            team = FullMatchSyncDto.LineupTeamDto(
                id = teamId,
                name = teamName,
                logo = "$teamName.png",
                colors = FullMatchSyncDto.LineupTeamDto.ColorsDto(
                    player = FullMatchSyncDto.LineupTeamDto.ColorsDto.ColorDetailDto(
                        primary = "#FF0000",
                        number = "#FFFFFF",
                        border = "#000000"
                    ),
                    goalkeeper = FullMatchSyncDto.LineupTeamDto.ColorsDto.ColorDetailDto(
                        primary = "#00FF00",
                        number = "#FFFFFF",
                        border = "#000000"
                    )
                )
            ),
            coach = FullMatchSyncDto.LineupDto.CoachDto(id = 9001L, name = "Coach", photo = null),
            formation = formation,
            startXI = startXI,
            substitutes = substitutes
        )
    }

    private fun createLineupWithNullPlayerNames(teamId: Long, teamName: String, formation: String): FullMatchSyncDto.LineupDto {
        val startXI = (1..11).map { index ->
            FullMatchSyncDto.LineupDto.LineupPlayerDto(
                FullMatchSyncDto.LineupDto.LineupPlayerDto.LineupPlayerDetailDto(
                    id = teamId * 100 + index, // 팀별 고유 ID
                    name = if (index % 3 == 0) null else "Player $index", // 일부 선수 이름을 null로 설정
                    number = index,
                    pos = if (index == 1) "G" else if (index <= 4) "D" else if (index <= 8) "M" else "F",
                    grid = "${index}:${index}"
                )
            )
        }

        return FullMatchSyncDto.LineupDto(
            team = FullMatchSyncDto.LineupTeamDto(id = teamId, name = teamName, logo = "$teamName.png"),
            coach = FullMatchSyncDto.LineupDto.CoachDto(id = 9001L, name = "Coach", photo = null),
            formation = formation,
            startXI = startXI,
            substitutes = emptyList()
        )
    }

    private fun createLineupWithEmptyPlayerNames(teamId: Long, teamName: String, formation: String): FullMatchSyncDto.LineupDto {
        val startXI = (1..11).map { index ->
            FullMatchSyncDto.LineupDto.LineupPlayerDto(
                FullMatchSyncDto.LineupDto.LineupPlayerDto.LineupPlayerDetailDto(
                    id = teamId * 100 + index, // 팀별 고유 ID
                    name = if (index % 3 == 0) "" else "Player $index", // 일부 선수 이름을 빈 문자열로 설정
                    number = index,
                    pos = if (index == 1) "G" else if (index <= 4) "D" else if (index <= 8) "M" else "F",
                    grid = "${index}:${index}"
                )
            )
        }

        return FullMatchSyncDto.LineupDto(
            team = FullMatchSyncDto.LineupTeamDto(id = teamId, name = teamName, logo = "$teamName.png"),
            coach = FullMatchSyncDto.LineupDto.CoachDto(id = 9001L, name = "Coach", photo = null),
            formation = formation,
            startXI = startXI,
            substitutes = emptyList()
        )
    }

    private fun createLineupWithNullPlayerIds(teamId: Long, teamName: String, formation: String): FullMatchSyncDto.LineupDto {
        val startXI = (1..11).map { index ->
            FullMatchSyncDto.LineupDto.LineupPlayerDto(
                FullMatchSyncDto.LineupDto.LineupPlayerDto.LineupPlayerDetailDto(
                    id = if (index % 3 == 0) null else teamId * 100 + index, // 일부 선수 ID를 null로 설정, 나머지는 팀별 고유 ID
                    name = "Player $index",
                    number = index,
                    pos = if (index == 1) "G" else if (index <= 4) "D" else if (index <= 8) "M" else "F",
                    grid = "${index}:${index}"
                )
            )
        }

        return FullMatchSyncDto.LineupDto(
            team = FullMatchSyncDto.LineupTeamDto(id = teamId, name = teamName, logo = "$teamName.png"),
            coach = FullMatchSyncDto.LineupDto.CoachDto(id = 9001L, name = "Coach", photo = null),
            formation = formation,
            startXI = startXI,
            substitutes = emptyList()
        )
    }

    private fun createLineupWithNullPlayerPositions(teamId: Long, teamName: String, formation: String): FullMatchSyncDto.LineupDto {
        val startXI = (1..11).map { index ->
            FullMatchSyncDto.LineupDto.LineupPlayerDto(
                FullMatchSyncDto.LineupDto.LineupPlayerDto.LineupPlayerDetailDto(
                    id = teamId * 100 + index, // 팀별 고유 ID
                    name = "Player $index",
                    number = index,
                    pos = if (index % 3 == 0) null else if (index == 1) "G" else if (index <= 4) "D" else if (index <= 8) "M" else "F", // 일부 포지션을 null로 설정
                    grid = "${index}:${index}"
                )
            )
        }

        return FullMatchSyncDto.LineupDto(
            team = FullMatchSyncDto.LineupTeamDto(id = teamId, name = teamName, logo = "$teamName.png"),
            coach = FullMatchSyncDto.LineupDto.CoachDto(id = 9001L, name = "Coach", photo = null),
            formation = formation,
            startXI = startXI,
            substitutes = emptyList()
        )
    }

    private fun createLineupWithAbnormalPlayerCounts(teamId: Long, teamName: String, formation: String): FullMatchSyncDto.LineupDto {
        val startXI = (1..9).map { index -> // 11명이 아닌 9명으로 설정
            FullMatchSyncDto.LineupDto.LineupPlayerDto(
                FullMatchSyncDto.LineupDto.LineupPlayerDto.LineupPlayerDetailDto(
                    id = teamId * 100 + index, // 팀별 고유 ID
                    name = "Player $index",
                    number = index,
                    pos = if (index == 1) "G" else if (index <= 3) "D" else if (index <= 6) "M" else "F",
                    grid = "${index}:${index}"
                )
            )
        }

        return FullMatchSyncDto.LineupDto(
            team = FullMatchSyncDto.LineupTeamDto(id = teamId, name = teamName, logo = "$teamName.png"),
            coach = FullMatchSyncDto.LineupDto.CoachDto(id = 9001L, name = "Coach", photo = null),
            formation = formation,
            startXI = startXI,
            substitutes = emptyList()
        )
    }

    private fun createLineupWithAllMissingPlayerInfo(teamId: Long, teamName: String, formation: String): FullMatchSyncDto.LineupDto {
        val startXI = (1..11).map { index ->
            FullMatchSyncDto.LineupDto.LineupPlayerDto(
                FullMatchSyncDto.LineupDto.LineupPlayerDto.LineupPlayerDetailDto(
                    id = null,
                    name = null, // 모든 선수 이름을 null로 설정
                    number = index,
                    pos = null,
                    grid = null
                )
            )
        }

        return FullMatchSyncDto.LineupDto(
            team = FullMatchSyncDto.LineupTeamDto(id = teamId, name = teamName, logo = "$teamName.png"),
            coach = FullMatchSyncDto.LineupDto.CoachDto(id = 9001L, name = "Coach", photo = null),
            formation = formation,
            startXI = startXI,
            substitutes = emptyList()
        )
    }

    private fun createLineupWithDuplicatePlayerNames(teamId: Long, teamName: String, formation: String): FullMatchSyncDto.LineupDto {
        val startXI = (1..11).map { index ->
            FullMatchSyncDto.LineupDto.LineupPlayerDto(
                FullMatchSyncDto.LineupDto.LineupPlayerDto.LineupPlayerDetailDto(
                    id = teamId * 100 + index, // 팀별 고유 ID
                    name = if (index <= 3) "John Doe" else "Player $index", // 처음 3명을 같은 이름으로 설정
                    number = index,
                    pos = if (index == 1) "G" else if (index <= 4) "D" else if (index <= 8) "M" else "F",
                    grid = "${index}:${index}"
                )
            )
        }

        return FullMatchSyncDto.LineupDto(
            team = FullMatchSyncDto.LineupTeamDto(id = teamId, name = teamName, logo = "$teamName.png"),
            coach = FullMatchSyncDto.LineupDto.CoachDto(id = 9001L, name = "Coach", photo = null),
            formation = formation,
            startXI = startXI,
            substitutes = emptyList()
        )
    }
}