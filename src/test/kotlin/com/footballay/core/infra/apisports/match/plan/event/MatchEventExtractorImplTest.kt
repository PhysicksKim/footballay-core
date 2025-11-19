package com.footballay.core.infra.apisports.match.plan.event

import com.footballay.core.infra.apisports.match.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.plan.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.plan.context.MatchPlayerKeyGenerator
import com.footballay.core.logger
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

class MatchEventExtractorImplTest {
    val log = logger()

    private lateinit var matchEventExtractorImpl: MatchEventExtractorImpl
    private lateinit var context: MatchPlayerContext

    @BeforeEach
    fun setUp() {
        matchEventExtractorImpl = MatchEventExtractorImpl()
        context = MatchPlayerContext()
    }

    @Test
    @DisplayName("빈 이벤트 리스트를 처리할 수 있다")
    fun `should handle empty events list`() {
        // given
        val dto = createDtoWithEmptyEvents()

        // when
        val result = matchEventExtractorImpl.extractEvents(dto, context)

        // then
        assertTrue(result.events.isEmpty())
    }

    @Test
    @DisplayName("팀 ID가 null인 경우 빈 결과를 반환한다")
    fun `should return empty result when team IDs are null`() {
        // given
        val dto = createDtoWithNullTeamIds()

        // when
        val result = matchEventExtractorImpl.extractEvents(dto, context)

        // then
        assertTrue(result.events.isEmpty())
    }

    @Test
    @DisplayName("라인업이 비어있는 경우 빈 결과를 반환한다")
    fun `should return empty result when lineups are empty`() {
        // given
        val dto = createDtoWithEmptyLineups()

        // when
        val result = matchEventExtractorImpl.extractEvents(dto, context)

        // then
        assertTrue(result.events.isEmpty())
    }

    @Test
    @DisplayName("정상적인 subst 이벤트를 정규화할 수 있다 - player가 후보, assist가 선발")
    fun `should normalize subst event when player is sub and assist is starter`() {
        // given
        val dto = createDtoWithNormalSubstEvent()

        // when
        val result = matchEventExtractorImpl.extractEvents(dto, context)

        // then
        assertEquals(1, result.events.size)

        val event = result.events[0]
        assertEquals("subst", event.eventType)
        // player는 후보 선수 (sub-in), assist는 선발 선수 (sub-out)
        assertEquals("${MatchPlayerKeyGenerator.ID_PREFIX}10012", event.playerMpKey) // 후보 선수
        assertEquals("${MatchPlayerKeyGenerator.ID_PREFIX}10001", event.assistMpKey) // 선발 선수
    }

    @Test
    @DisplayName("정상적인 subst 이벤트를 정규화할 수 있다 - player가 선발, assist가 후보")
    fun `should normalize subst event when player is starter and assist is sub`() {
        // given
        val dto = createDtoWithReversedSubstEvent()

        // when
        val result = matchEventExtractorImpl.extractEvents(dto, context)

        // then
        assertEquals(1, result.events.size)

        val event = result.events[0]
        assertEquals("subst", event.eventType)
        // 정규화 후: player는 후보 선수 (sub-in), assist는 선발 선수 (sub-out)
        assertEquals("${MatchPlayerKeyGenerator.ID_PREFIX}10012", event.playerMpKey) // 후보 선수 (정규화됨)
        assertEquals("${MatchPlayerKeyGenerator.ID_PREFIX}10001", event.assistMpKey) // 선발 선수 (정규화됨)
    }

    @Test
    @DisplayName("골 이벤트는 정규화하지 않고 그대로 처리한다")
    fun `should not normalize goal event`() {
        // given
        val dto = createDtoWithGoalEvent()

        // when
        val result = matchEventExtractorImpl.extractEvents(dto, context)

        // then
        assertEquals(1, result.events.size)

        val event = result.events[0]
        assertEquals("goal", event.eventType)
        assertEquals("${MatchPlayerKeyGenerator.ID_PREFIX}10001", event.playerMpKey) // 그대로 유지
        assertEquals("${MatchPlayerKeyGenerator.ID_PREFIX}10002", event.assistMpKey) // 그대로 유지
    }

    @Test
    @DisplayName("라인업에 없는 이벤트 전용 선수를 nonLineupDtoMap에 추가한다")
    fun `should add event-only players to nonLineupDtoMap`() {
        // given
        val dto = createDtoWithEventOnlyPlayer()

        // when
        val result = matchEventExtractorImpl.extractEvents(dto, context)

        // then
        assertEquals(1, result.events.size)

        // Context에 이벤트 전용 선수가 추가되었는지 확인
        assertTrue(context.eventMpDtoMap.isNotEmpty())
        val eventOnlyPlayerKey = "${MatchPlayerKeyGenerator.ID_PREFIX}99999"
        assertTrue(context.eventMpDtoMap.containsKey(eventOnlyPlayerKey))

        val eventOnlyPlayer = context.eventMpDtoMap[eventOnlyPlayerKey]
        assertNotNull(eventOnlyPlayer)
        assertEquals("Event Only Player", eventOnlyPlayer!!.name)
        assertEquals(99999L, eventOnlyPlayer.apiId)
        assertTrue(eventOnlyPlayer.substitute) // 이벤트용 선수는 substitute로 설정
    }

    @Test
    @DisplayName("여러 이벤트를 순서대로 처리할 수 있다")
    fun `should process multiple events in order`() {
        // given
        val dto = createDtoWithMultipleEvents()

        // when
        val result = matchEventExtractorImpl.extractEvents(dto, context)

        // then
        assertEquals(3, result.events.size)

        // 순서 확인
        val event1 = result.events[0]
        val event2 = result.events[1]
        val event3 = result.events[2]

        assertEquals(0, event1.sequence)
        assertEquals("goal", event1.eventType)
        assertEquals(1, event2.sequence)
        assertEquals("subst", event2.eventType)
        assertEquals(2, event3.sequence)
        assertEquals("card", event3.eventType)
    }

    @Test
    @DisplayName("복잡한 교체 시나리오를 처리할 수 있다 - 연속 교체")
    fun `should handle complex substitution scenario - consecutive substitutions`() {
        // given
        val dto = createDtoWithConsecutiveSubstitutions()

        // when
        val result = matchEventExtractorImpl.extractEvents(dto, context)

        // then
        assertEquals(2, result.events.size)

        // 첫 번째 교체: Player 1 OUT, Sub Player 12 IN
        val firstSubst = result.events[0]
        assertEquals("subst", firstSubst.eventType)
        assertEquals("${MatchPlayerKeyGenerator.ID_PREFIX}10012", firstSubst.playerMpKey) // sub-in
        assertEquals("${MatchPlayerKeyGenerator.ID_PREFIX}10001", firstSubst.assistMpKey) // sub-out

        // 두 번째 교체: Sub Player 12 OUT, Sub Player 13 IN (이미 교체된 선수가 다시 교체됨)
        val secondSubst = result.events[1]
        assertEquals("subst", secondSubst.eventType)
        assertEquals("${MatchPlayerKeyGenerator.ID_PREFIX}10013", secondSubst.playerMpKey) // sub-in
        assertEquals("${MatchPlayerKeyGenerator.ID_PREFIX}10012", secondSubst.assistMpKey) // sub-out (이미 교체된 선수)
    }

    @Test
    @DisplayName("비정상적인 subst 이벤트를 처리할 수 있다 - 둘 다 후보 선수")
    fun `should handle abnormal subst event - both players are substitutes`() {
        // given
        val dto = createDtoWithAbnormalSubstEvent()

        // when
        val result = matchEventExtractorImpl.extractEvents(dto, context)

        // then
        assertEquals(1, result.events.size)

        val event = result.events[0]
        assertEquals("subst", event.eventType)
        // 비정상적인 경우 원본 순서 유지
        assertEquals("${MatchPlayerKeyGenerator.ID_PREFIX}10012", event.playerMpKey)
        assertEquals("${MatchPlayerKeyGenerator.ID_PREFIX}10013", event.assistMpKey)
    }

    @Test
    @DisplayName("이벤트에서 null player/assist를 처리할 수 있다")
    fun `should handle null player and assist in events`() {
        // given
        val dto = createDtoWithNullPlayerAssist()

        // when
        val result = matchEventExtractorImpl.extractEvents(dto, context)

        // then
        assertEquals(1, result.events.size)

        val event = result.events[0]
        assertEquals("card", event.eventType)
        assertNull(event.playerMpKey)
        assertNull(event.assistMpKey)
    }

    @Test
    @DisplayName("subst 이벤트에서 name 이 null 인 경우 MatchPlayer 를 저장하지 않는다")
    fun `should handle null player and assist names in subst events`() {
        // given
        val dto = createDtoWithNullPlayerAssistNames()

        // when
        val result = matchEventExtractorImpl.extractEvents(dto, context)

        // then
        assertEquals(1, result.events.size)

        val event = result.events[0]
        assertEquals("subst", event.eventType)
        // null name이 있는 경우 원본 이벤트 그대로 유지
        assertEquals("${MatchPlayerKeyGenerator.ID_PREFIX}10001", event.playerMpKey)
        assertEquals(null, event.assistMpKey)
    }

    @Test
    @DisplayName("여러 팀의 교체 이벤트를 처리할 수 있다")
    fun `should handle substitution events from multiple teams`() {
        // given
        val dto = createDtoWithMultiTeamSubstitutions()

        // when
        val result = matchEventExtractorImpl.extractEvents(dto, context)

        // then
        assertEquals(2, result.events.size)

        // 홈팀 교체
        val homeSubst = result.events[0]
        assertEquals("subst", homeSubst.eventType)
        assertEquals(100L, homeSubst.teamApiId)
        assertEquals("${MatchPlayerKeyGenerator.ID_PREFIX}10012", homeSubst.playerMpKey)
        assertEquals("${MatchPlayerKeyGenerator.ID_PREFIX}10001", homeSubst.assistMpKey)

        // 어웨이팀 교체
        val awaySubst = result.events[1]
        assertEquals("subst", awaySubst.eventType)
        assertEquals(200L, awaySubst.teamApiId)
        assertEquals("${MatchPlayerKeyGenerator.ID_PREFIX}20012", awaySubst.playerMpKey)
        assertEquals("${MatchPlayerKeyGenerator.ID_PREFIX}20001", awaySubst.assistMpKey)
    }

    // Test Data 생성 메서드들

    private fun createDtoWithEmptyEvents(): FullMatchSyncDto =
        FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = emptyList(),
            lineups = createNormalLineups(),
            statistics = emptyList(),
            players = emptyList(),
        )

    private fun createDtoWithNullTeamIds(): FullMatchSyncDto =
        FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams =
                FullMatchSyncDto.TeamsDto(
                    home =
                        FullMatchSyncDto.TeamsDto.TeamDto(
                            id = null,
                            name = "Arsenal",
                            logo = "arsenal.png",
                            winner = null,
                        ),
                    away =
                        FullMatchSyncDto.TeamsDto.TeamDto(
                            id = null,
                            name = "Manchester City",
                            logo = "city.png",
                            winner = null,
                        ),
                ),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = listOf(createGoalEvent()),
            lineups = createNormalLineups(),
            statistics = emptyList(),
            players = emptyList(),
        )

    private fun createDtoWithEmptyLineups(): FullMatchSyncDto =
        FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = listOf(createGoalEvent()),
            lineups = emptyList(),
            statistics = emptyList(),
            players = emptyList(),
        )

    private fun createDtoWithNormalSubstEvent(): FullMatchSyncDto {
        val events =
            listOf(
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 45, extra = 2),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10012L, name = "Sub Player 12"), // 후보 선수
                    assist = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10001L, name = "Player 1"), // 선발 선수
                    type = "subst",
                    detail = "Substitution",
                    comments = "Player 1 out, Sub Player 12 in",
                ),
            )

        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = events,
            lineups = createNormalLineups(),
            statistics = emptyList(),
            players = emptyList(),
        )
    }

    private fun createDtoWithReversedSubstEvent(): FullMatchSyncDto {
        val events =
            listOf(
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 45, extra = 2),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10001L, name = "Player 1"), // 선발 선수
                    assist = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10012L, name = "Sub Player 12"), // 후보 선수
                    type = "subst",
                    detail = "Substitution",
                    comments = "Player 1 out, Sub Player 12 in",
                ),
            )

        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = events,
            lineups = createNormalLineups(),
            statistics = emptyList(),
            players = emptyList(),
        )
    }

    private fun createDtoWithGoalEvent(): FullMatchSyncDto {
        val events = listOf(createGoalEvent())

        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = events,
            lineups = createNormalLineups(),
            statistics = emptyList(),
            players = emptyList(),
        )
    }

    private fun createDtoWithEventOnlyPlayer(): FullMatchSyncDto {
        val events =
            listOf(
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 67, extra = null),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 99999L, name = "Event Only Player"),
                    assist = null,
                    type = "card",
                    detail = "Yellow Card",
                    comments = "Foul play",
                ),
            )

        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = events,
            lineups = createNormalLineups(),
            statistics = emptyList(),
            players = emptyList(),
        )
    }

    private fun createDtoWithMultipleEvents(): FullMatchSyncDto {
        val events =
            listOf(
                createGoalEvent(),
                createNormalSubstEvent(),
                createCardEvent(),
            )

        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = events,
            lineups = createNormalLineups(),
            statistics = emptyList(),
            players = emptyList(),
        )
    }

    private fun createDtoWithConsecutiveSubstitutions(): FullMatchSyncDto {
        val events =
            listOf(
                // 첫 번째 교체: Player 1 OUT, Sub Player 12 IN
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 45, extra = 2),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10012L, name = "Sub Player 12"), // 후보 선수
                    assist = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10001L, name = "Player 1"), // 선발 선수
                    type = "subst",
                    detail = "Substitution",
                    comments = "Player 1 out, Sub Player 12 in",
                ),
                // 두 번째 교체: Sub Player 12 OUT, Sub Player 13 IN (이미 교체된 선수가 다시 교체됨)
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 67, extra = null),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10013L, name = "Sub Player 13"), // 후보 선수
                    assist = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10012L, name = "Sub Player 12"), // 이미 교체된 선수
                    type = "subst",
                    detail = "Substitution",
                    comments = "Sub Player 12 out, Sub Player 13 in",
                ),
            )

        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = events,
            lineups = createNormalLineups(),
            statistics = emptyList(),
            players = emptyList(),
        )
    }

    private fun createDtoWithAbnormalSubstEvent(): FullMatchSyncDto {
        val events =
            listOf(
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 45, extra = 2),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10012L, name = "Sub Player 12"), // 후보 선수
                    assist = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10013L, name = "Sub Player 13"), // 후보 선수
                    type = "subst",
                    detail = "Substitution",
                    comments = "Abnormal substitution",
                ),
            )

        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = events,
            lineups = createNormalLineups(),
            statistics = emptyList(),
            players = emptyList(),
        )
    }

    private fun createDtoWithNullPlayerAssist(): FullMatchSyncDto {
        val events =
            listOf(
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 67, extra = null),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    player = null,
                    assist = null,
                    type = "card",
                    detail = "Yellow Card",
                    comments = "Team foul",
                ),
            )

        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = events,
            lineups = createNormalLineups(),
            statistics = emptyList(),
            players = emptyList(),
        )
    }

    private fun createDtoWithNullPlayerAssistNames(): FullMatchSyncDto {
        val events =
            listOf(
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 45, extra = 2),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10001L, name = "Player 1"),
                    assist = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10012L, name = null), // null name
                    type = "subst",
                    detail = "Substitution",
                    comments = "Substitution with null name",
                ),
            )

        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = events,
            lineups = createNormalLineups(),
            statistics = emptyList(),
            players = emptyList(),
        )
    }

    private fun createDtoWithMultiTeamSubstitutions(): FullMatchSyncDto {
        val events =
            listOf(
                // 홈팀 교체
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 45, extra = 2),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10012L, name = "Sub Player 12"),
                    assist = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10001L, name = "Player 1"),
                    type = "subst",
                    detail = "Substitution",
                    comments = "Home team substitution",
                ),
                // 어웨이팀 교체
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 67, extra = null),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 200L, name = "Manchester City", logo = "city.png"),
                    player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 20012L, name = "Sub Player 12"),
                    assist = FullMatchSyncDto.EventDto.EventPlayerDto(id = 20001L, name = "Player 1"),
                    type = "subst",
                    detail = "Substitution",
                    comments = "Away team substitution",
                ),
            )

        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 2, away = 1),
            score = createNormalScore(),
            events = events,
            lineups = createNormalLineups(),
            statistics = emptyList(),
            players = emptyList(),
        )
    }

    private fun createGoalEvent(): FullMatchSyncDto.EventDto =
        FullMatchSyncDto.EventDto(
            time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 23, extra = null),
            team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
            player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10001L, name = "Player 1"),
            assist = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10002L, name = "Player 2"),
            type = "goal",
            detail = "Goal",
            comments = "Beautiful goal!",
        )

    private fun createNormalSubstEvent(): FullMatchSyncDto.EventDto =
        FullMatchSyncDto.EventDto(
            time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 45, extra = 2),
            team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
            player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10012L, name = "Sub Player 12"),
            assist = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10001L, name = "Player 1"),
            type = "subst",
            detail = "Substitution",
            comments = "Player 1 out, Sub Player 12 in",
        )

    private fun createCardEvent(): FullMatchSyncDto.EventDto =
        FullMatchSyncDto.EventDto(
            time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 67, extra = null),
            team = FullMatchSyncDto.TeamSimpleDto(id = 200L, name = "Manchester City", logo = "city.png"),
            player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 20001L, name = "Player 1"),
            assist = null,
            type = "card",
            detail = "Yellow Card",
            comments = "Foul play",
        )

    private fun createNormalFixture(): FullMatchSyncDto.FixtureDto =
        FullMatchSyncDto.FixtureDto(
            id = 12345L,
            referee = "Mike Dean",
            timezone = "Europe/London",
            date = OffsetDateTime.of(2024, 1, 15, 20, 0, 0, 0, ZoneOffset.UTC),
            timestamp = 1705334400L,
            periods = FullMatchSyncDto.FixtureDto.PeriodsDto(first = 1705334400L, second = 1705338000L),
            venue = FullMatchSyncDto.FixtureDto.VenueDto(id = 1L, name = "Emirates Stadium", city = "London"),
            status =
                FullMatchSyncDto.FixtureDto.StatusDto(
                    long = "Match Finished",
                    short = "FT",
                    elapsed = 90,
                    extra = null,
                ),
        )

    private fun createNormalLeague(): FullMatchSyncDto.LeagueDto =
        FullMatchSyncDto.LeagueDto(
            id = 39L,
            name = "Premier League",
            country = "England",
            logo = "premier-league.png",
            flag = "england.png",
            season = 2024,
            round = "Regular Season - 20",
            standings = true,
        )

    private fun createNormalTeams(): FullMatchSyncDto.TeamsDto =
        FullMatchSyncDto.TeamsDto(
            home = FullMatchSyncDto.TeamsDto.TeamDto(id = 100L, name = "Arsenal", logo = "arsenal.png", winner = true),
            away =
                FullMatchSyncDto.TeamsDto.TeamDto(
                    id = 200L,
                    name = "Manchester City",
                    logo = "city.png",
                    winner = false,
                ),
        )

    private fun createNormalScore(): FullMatchSyncDto.ScoreDto =
        FullMatchSyncDto.ScoreDto(
            halftime = FullMatchSyncDto.ScoreDto.PairDto(home = 1, away = 0),
            fulltime = FullMatchSyncDto.ScoreDto.PairDto(home = 2, away = 1),
            extratime = null,
            penalty = null,
        )

    private fun createNormalLineups(): List<FullMatchSyncDto.LineupDto> =
        listOf(
            createLineupWithTeamId(100L, "Arsenal", "4-3-3"),
            createLineupWithTeamId(200L, "Manchester City", "4-2-3-1"),
        )

    private fun createLineupWithTeamId(
        teamId: Long,
        teamName: String,
        formation: String,
    ): FullMatchSyncDto.LineupDto {
        // 팀별 고유한 선수 ID 생성: teamId * 100 + index
        val startXI =
            (1..11).map { index ->
                FullMatchSyncDto.LineupDto.LineupPlayerDto(
                    FullMatchSyncDto.LineupDto.LineupPlayerDto.LineupPlayerDetailDto(
                        id = teamId * 100 + index, // 팀별 고유 ID: 100팀은 10001~10011, 200팀은 20001~20011
                        name = "Player $index",
                        number = index,
                        pos =
                            if (index == 1) {
                                "G"
                            } else if (index <= 4) {
                                "D"
                            } else if (index <= 8) {
                                "M"
                            } else {
                                "F"
                            },
                        grid = "$index:$index",
                    ),
                )
            }

        val substitutes =
            (12..18).map { index ->
                FullMatchSyncDto.LineupDto.LineupPlayerDto(
                    FullMatchSyncDto.LineupDto.LineupPlayerDto.LineupPlayerDetailDto(
                        id = teamId * 100 + index, // 후보 선수: 100팀은 10012~10018, 200팀은 20012~20018
                        name = "Sub Player $index",
                        number = index,
                        pos =
                            if (index == 12) {
                                "G"
                            } else if (index <= 14) {
                                "D"
                            } else if (index <= 16) {
                                "M"
                            } else {
                                "F"
                            },
                        grid = null,
                    ),
                )
            }

        return FullMatchSyncDto.LineupDto(
            team =
                FullMatchSyncDto.LineupTeamDto(
                    id = teamId,
                    name = teamName,
                    logo = "$teamName.png",
                    colors =
                        FullMatchSyncDto.LineupTeamDto.ColorsDto(
                            player =
                                FullMatchSyncDto.LineupTeamDto.ColorsDto.ColorDetailDto(
                                    primary = "#FF0000",
                                    number = "#FFFFFF",
                                    border = "#000000",
                                ),
                            goalkeeper =
                                FullMatchSyncDto.LineupTeamDto.ColorsDto.ColorDetailDto(
                                    primary = "#00FF00",
                                    number = "#FFFFFF",
                                    border = "#000000",
                                ),
                        ),
                ),
            coach = FullMatchSyncDto.LineupDto.CoachDto(id = 9001L, name = "Coach", photo = null),
            formation = formation,
            startXI = startXI,
            substitutes = substitutes,
        )
    }

    @Test
    @DisplayName("패널티 실축 시 type을 ETC로 변경한다")
    fun `should change type to ETC for missed penalty`() {
        // given
        val dto = createDtoWithMissedPenalty()

        // when
        val result = matchEventExtractorImpl.extractEvents(dto, context)

        // then
        assertEquals(1, result.events.size)

        val event = result.events[0]
        assertEquals("ETC", event.eventType) // Goal이 아닌 ETC
        assertEquals("Missed Penalty", event.detail)
        assertEquals("${MatchPlayerKeyGenerator.ID_PREFIX}10001", event.playerMpKey)
    }

    @Test
    @DisplayName("Own Goal 이벤트의 팀을 상대 팀(득점 획득 팀)으로 변경한다")
    fun `should change team to opponent for own goal`() {
        // given
        val dto = createDtoWithOwnGoal()

        // when
        val result = matchEventExtractorImpl.extractEvents(dto, context)

        // then
        assertEquals(1, result.events.size)

        val event = result.events[0]
        assertEquals("Goal", event.eventType)
        assertEquals("Own Goal", event.detail)
        // 홈팀 선수(100L)가 Own Goal을 넣었으므로 teamApiId는 어웨이팀(200L)이어야 함
        assertEquals(200L, event.teamApiId)
    }

    @Test
    @DisplayName("Substitution에서 player와 assist가 모두 완전히 null인 경우 UNKNOWN으로 처리한다")
    fun `should change type to UNKNOWN for substitution with both null players`() {
        // given
        val dto = createDtoWithSubstBothNull()

        // when
        val result = matchEventExtractorImpl.extractEvents(dto, context)

        // then
        assertEquals(1, result.events.size)

        val event = result.events[0]
        assertEquals("UNKNOWN", event.eventType) // subst가 아닌 UNKNOWN
        assertEquals("Substitution 1", event.detail)
        assertNull(event.playerMpKey)
        assertNull(event.assistMpKey)
    }

    @Test
    @DisplayName("이벤트 정렬: elapsed 시간 순서대로 정렬된다")
    fun `should sort events by elapsed time`() {
        // given
        val dto = createDtoWithEventsInWrongOrder()

        // when
        val result = matchEventExtractorImpl.extractEvents(dto, context)

        // then
        assertEquals(3, result.events.size)

        // elapsed 기준으로 정렬되어야 함
        assertEquals(10, result.events[0].elapsedTime)
        assertEquals(20, result.events[1].elapsedTime)
        assertEquals(30, result.events[2].elapsedTime)

        // sequence는 0부터 순차적으로 재할당되어야 함
        assertEquals(0, result.events[0].sequence)
        assertEquals(1, result.events[1].sequence)
        assertEquals(2, result.events[2].sequence)
    }

    @Test
    @DisplayName("이벤트 정렬: elapsed가 같으면 extraTime 순서대로 정렬된다")
    fun `should sort events by extraTime when elapsed is same`() {
        // given
        val dto = createDtoWithSameElapsedDifferentExtraTime()

        // when
        val result = matchEventExtractorImpl.extractEvents(dto, context)

        // then
        assertEquals(3, result.events.size)

        // elapsed가 45로 동일하므로 extraTime 기준으로 정렬
        assertEquals(45, result.events[0].elapsedTime)
        assertEquals(null, result.events[0].extraTime) // null은 0으로 간주
        assertEquals(45, result.events[1].elapsedTime)
        assertEquals(2, result.events[1].extraTime)
        assertEquals(45, result.events[2].elapsedTime)
        assertEquals(5, result.events[2].extraTime)
    }

    @Test
    @DisplayName("이벤트 정렬: elapsed와 extraTime이 같고 팀도 같으면 Substitution 번호 순서대로 정렬된다")
    fun `should sort events by substitution number when elapsed, extraTime and team are same`() {
        // given
        val dto = createDtoWithSameTimeTeamDifferentSubstNumber()

        // when
        val result = matchEventExtractorImpl.extractEvents(dto, context)

        // then
        assertEquals(3, result.events.size)

        // elapsed=56, extraTime=null, team=100L로 모두 동일하므로 Substitution 번호 순서대로 정렬
        assertEquals("Substitution 1", result.events[0].detail)
        assertEquals("Substitution 2", result.events[1].detail)
        assertEquals("Substitution 3", result.events[2].detail)
    }

    // 새로운 테스트 데이터 생성 메서드들

    private fun createDtoWithMissedPenalty(): FullMatchSyncDto {
        val events =
            listOf(
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 13, extra = null),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10001L, name = "Player 1"),
                    assist = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10002L, name = "Player 2"),
                    type = "Goal",
                    detail = "Missed Penalty",
                    comments = null,
                ),
            )

        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 0, away = 0),
            score = createNormalScore(),
            events = events,
            lineups = createNormalLineups(),
            statistics = emptyList(),
            players = emptyList(),
        )
    }

    private fun createDtoWithOwnGoal(): FullMatchSyncDto {
        val events =
            listOf(
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 25, extra = null),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"), // 홈팀
                    player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10001L, name = "Player 1"), // 홈팀 선수
                    assist = null,
                    type = "Goal",
                    detail = "Own Goal",
                    comments = null,
                ),
            )

        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 0, away = 1), // 어웨이팀이 득점
            score = createNormalScore(),
            events = events,
            lineups = createNormalLineups(),
            statistics = emptyList(),
            players = emptyList(),
        )
    }

    private fun createDtoWithSubstBothNull(): FullMatchSyncDto {
        val events =
            listOf(
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 70, extra = null),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    player = null, // 완전히 null
                    assist = null, // 완전히 null
                    type = "subst",
                    detail = "Substitution 1",
                    comments = null,
                ),
            )

        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 0, away = 0),
            score = createNormalScore(),
            events = events,
            lineups = createNormalLineups(),
            statistics = emptyList(),
            players = emptyList(),
        )
    }

    private fun createDtoWithEventsInWrongOrder(): FullMatchSyncDto {
        val events =
            listOf(
                // 역순으로 배치
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 30, extra = null),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10003L, name = "Player 3"),
                    assist = null,
                    type = "card",
                    detail = "Yellow Card",
                    comments = null,
                ),
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 10, extra = null),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10001L, name = "Player 1"),
                    assist = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10002L, name = "Player 2"),
                    type = "goal",
                    detail = "Normal Goal",
                    comments = null,
                ),
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 20, extra = null),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10002L, name = "Player 2"),
                    assist = null,
                    type = "card",
                    detail = "Yellow Card",
                    comments = null,
                ),
            )

        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 1, away = 0),
            score = createNormalScore(),
            events = events,
            lineups = createNormalLineups(),
            statistics = emptyList(),
            players = emptyList(),
        )
    }

    private fun createDtoWithSameElapsedDifferentExtraTime(): FullMatchSyncDto {
        val events =
            listOf(
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 45, extra = 5),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10003L, name = "Player 3"),
                    assist = null,
                    type = "card",
                    detail = "Yellow Card",
                    comments = null,
                ),
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 45, extra = null), // null은 0으로 간주
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10001L, name = "Player 1"),
                    assist = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10002L, name = "Player 2"),
                    type = "goal",
                    detail = "Normal Goal",
                    comments = null,
                ),
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 45, extra = 2),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10012L, name = "Sub Player 12"),
                    assist = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10001L, name = "Player 1"),
                    type = "subst",
                    detail = "Substitution 1",
                    comments = null,
                ),
            )

        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 1, away = 0),
            score = createNormalScore(),
            events = events,
            lineups = createNormalLineups(),
            statistics = emptyList(),
            players = emptyList(),
        )
    }

    private fun createDtoWithSameTimeTeamDifferentSubstNumber(): FullMatchSyncDto {
        val events =
            listOf(
                // 역순으로 배치
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 56, extra = null),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10014L, name = "Sub Player 14"),
                    assist = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10003L, name = "Player 3"),
                    type = "subst",
                    detail = "Substitution 3",
                    comments = null,
                ),
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 56, extra = null),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10012L, name = "Sub Player 12"),
                    assist = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10001L, name = "Player 1"),
                    type = "subst",
                    detail = "Substitution 1",
                    comments = null,
                ),
                FullMatchSyncDto.EventDto(
                    time = FullMatchSyncDto.EventDto.TimeDto(elapsed = 56, extra = null),
                    team = FullMatchSyncDto.TeamSimpleDto(id = 100L, name = "Arsenal", logo = "arsenal.png"),
                    player = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10013L, name = "Sub Player 13"),
                    assist = FullMatchSyncDto.EventDto.EventPlayerDto(id = 10002L, name = "Player 2"),
                    type = "subst",
                    detail = "Substitution 2",
                    comments = null,
                ),
            )

        return FullMatchSyncDto(
            fixture = createNormalFixture(),
            league = createNormalLeague(),
            teams = createNormalTeams(),
            goals = FullMatchSyncDto.GoalsDto(home = 0, away = 0),
            score = createNormalScore(),
            events = events,
            lineups = createNormalLineups(),
            statistics = emptyList(),
            players = emptyList(),
        )
    }
}
