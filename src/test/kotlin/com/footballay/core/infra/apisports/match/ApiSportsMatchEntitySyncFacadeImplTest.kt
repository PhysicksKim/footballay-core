package com.footballay.core.infra.apisports.match

import com.footballay.core.infra.apisports.match.plan.base.MatchBaseDtoExtractor
import com.footballay.core.infra.apisports.match.plan.dto.FixtureApiSportsDto
import com.footballay.core.infra.apisports.match.plan.dto.MatchEventPlanDto
import com.footballay.core.infra.apisports.match.plan.dto.MatchLineupPlanDto
import com.footballay.core.infra.apisports.match.plan.dto.MatchPlayerStatPlanDto
import com.footballay.core.infra.apisports.match.plan.dto.MatchTeamStatPlanDto
import com.footballay.core.infra.apisports.match.plan.event.MatchEventDtoExtractor
import com.footballay.core.infra.apisports.match.plan.lineup.MatchLineupDtoExtractor
import com.footballay.core.infra.apisports.match.plan.playerstat.MatchPlayerStatDtoExtractor
import com.footballay.core.infra.apisports.match.plan.teamstat.MatchTeamStatDtoExtractor
import com.footballay.core.infra.apisports.syncer.match.persist.result.MatchEntitySyncResult
import com.footballay.core.infra.apisports.syncer.match.persist.result.MatchEventSyncResult
import com.footballay.core.infra.apisports.syncer.match.persist.result.MatchPlayerSyncResult
import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import com.footballay.core.infra.match.FixtureStatusClassifier
import com.footballay.core.infra.persistence.core.entity.FixtureStatusCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
class ApiSportsMatchEntitySyncFacadeImplTest {
    @Mock
    private lateinit var baseDtoExtractor: MatchBaseDtoExtractor

    @Mock
    private lateinit var lineupDtoExtractor: MatchLineupDtoExtractor

    @Mock
    private lateinit var eventDtoExtractor: MatchEventDtoExtractor

    @Mock
    private lateinit var teamStatExtractor: MatchTeamStatDtoExtractor

    @Mock
    private lateinit var playerStatExtractor: MatchPlayerStatDtoExtractor

    @Mock
    private lateinit var matchEntityPersistManager: MatchEntityPersistManager

    private lateinit var facade: ApiSportsMatchEntitySyncFacadeImpl

    @BeforeEach
    fun setup() {
        facade =
            ApiSportsMatchEntitySyncFacadeImpl(
                baseDtoExtractor = baseDtoExtractor,
                lineupDtoExtractor = lineupDtoExtractor,
                eventDtoExtractor = eventDtoExtractor,
                teamStatExtractor = teamStatExtractor,
                playerStatExtractor = playerStatExtractor,
                matchEntityPersistManager = matchEntityPersistManager,
                fixtureStatusClassifier = FixtureStatusClassifier(),
            )

        whenever(baseDtoExtractor.extractBaseMatch(any())).thenReturn(FixtureApiSportsDto(apiId = 1L, referee = null, date = null, round = null))
        whenever(lineupDtoExtractor.extractLineup(any(), any())).thenReturn(MatchLineupPlanDto.EMPTY)
        whenever(eventDtoExtractor.extractEvents(any(), any())).thenReturn(MatchEventPlanDto())
        whenever(teamStatExtractor.extractTeamStats(any())).thenReturn(MatchTeamStatPlanDto.empty())
        whenever(playerStatExtractor.extractPlayerStats(any(), any())).thenReturn(MatchPlayerStatPlanDto.empty())
        whenever(matchEntityPersistManager.syncMatchEntities(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(
                MatchEntitySyncResult.success(
                    createdCount = 0,
                    retainedCount = 0,
                    deletedCount = 0,
                    playerChanges = MatchPlayerSyncResult.empty(),
                    eventChanges = MatchEventSyncResult.empty(),
                ),
            )
    }

    @Test
    fun `PST CANC ABD AWD WO는 NotPlayed를 반환한다`() {
        listOf("PST", "CANC", "ABD", "AWD", "WO").forEach { statusShort ->
            val result = facade.syncFixtureMatchEntities(createDto(statusShort = statusShort))

            assertThat(result)
                .describedAs("statusShort=$statusShort")
                .isInstanceOf(MatchDataSyncResult.NotPlayed::class.java)
            assertThat((result as MatchDataSyncResult.NotPlayed).statusCode).isEqualTo(FixtureStatusCode.fromString(statusShort))
        }
    }

    @Test
    fun `NS라도 kickoff이 지났으면 Live를 반환한다`() {
        val result =
            facade.syncFixtureMatchEntities(
                createDto(
                    statusShort = "NS",
                    kickoff = Instant.now().minusSeconds(60),
                ),
            )

        assertThat(result).isInstanceOf(MatchDataSyncResult.Live::class.java)
    }

    @Test
    fun `FT AET PEN은 PostMatch를 반환한다`() {
        listOf("FT", "AET", "PEN").forEach { statusShort ->
            val result = facade.syncFixtureMatchEntities(createDto(statusShort = statusShort, elapsed = 90))

            assertThat(result)
                .describedAs("statusShort=$statusShort")
                .isInstanceOf(MatchDataSyncResult.PostMatch::class.java)
        }
    }

    private fun createDto(
        statusShort: String,
        kickoff: Instant = Instant.now(),
        elapsed: Int? = null,
    ): FullMatchSyncDto =
        FullMatchSyncDto(
            fixture =
                FullMatchSyncDto.FixtureDto(
                    id = 1L,
                    referee = null,
                    timezone = "UTC",
                    date = OffsetDateTime.ofInstant(kickoff, ZoneOffset.UTC),
                    timestamp = kickoff.epochSecond,
                    periods = FullMatchSyncDto.FixtureDto.PeriodsDto(first = 0L, second = 0L),
                    venue = FullMatchSyncDto.FixtureDto.VenueDto(id = null, name = null, city = null),
                    status = FullMatchSyncDto.FixtureDto.StatusDto(long = statusShort, short = statusShort, elapsed = elapsed, extra = null),
                ),
            league =
                FullMatchSyncDto.LeagueDto(
                    id = 1L,
                    name = "League",
                    country = null,
                    logo = null,
                    flag = null,
                    season = 2026,
                    round = null,
                    standings = null,
                ),
            teams =
                FullMatchSyncDto.TeamsDto(
                    home = FullMatchSyncDto.TeamsDto.TeamDto(id = 1L, name = "Home", logo = "", winner = null),
                    away = FullMatchSyncDto.TeamsDto.TeamDto(id = 2L, name = "Away", logo = "", winner = null),
                ),
            goals = FullMatchSyncDto.GoalsDto(home = null, away = null),
            score = FullMatchSyncDto.ScoreDto(halftime = null, fulltime = null, extratime = null, penalty = null),
            events = emptyList(),
            lineups = emptyList(),
            statistics = emptyList(),
            players = emptyList(),
        )
}
