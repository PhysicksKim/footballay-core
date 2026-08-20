package com.footballay.core.infra.backbone.mock.scenario

import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.model.FixtureModel
import com.footballay.core.domain.model.LeagueModel
import com.footballay.core.domain.model.TeamModel
import com.footballay.core.infra.backbone.mock.resource.MockFixtureCreateCommand
import com.footballay.core.infra.backbone.mock.resource.MockFixtureService
import com.footballay.core.infra.backbone.mock.resource.MockLeagueCreateCommand
import com.footballay.core.infra.backbone.mock.resource.MockLeagueService
import com.footballay.core.infra.backbone.mock.resource.MockTeamCreateCommand
import com.footballay.core.infra.backbone.mock.resource.MockTeamService
import com.footballay.core.infra.backbone.mock.resource.MockUidFactory
import com.footballay.core.infra.persistence.mockbackbone.repository.MockBackboneFixtureRepository
import com.footballay.core.infra.persistence.mockbackbone.repository.MockBackboneLeagueRepository
import com.footballay.core.infra.persistence.mockbackbone.repository.MockBackboneTeamRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
class MockSimpleFixtureScenarioServiceTest {
    @Mock
    private lateinit var leagueService: MockLeagueService

    @Mock
    private lateinit var teamService: MockTeamService

    @Mock
    private lateinit var fixtureService: MockFixtureService

    @Mock
    private lateinit var mockLeagueRepository: MockBackboneLeagueRepository

    @Mock
    private lateinit var mockTeamRepository: MockBackboneTeamRepository

    @Mock
    private lateinit var mockFixtureRepository: MockBackboneFixtureRepository

    private val now = Instant.parse("2026-05-29T10:00:00Z")
    private lateinit var service: MockSimpleFixtureScenarioService

    @BeforeEach
    fun setUp() {
        service =
            MockSimpleFixtureScenarioService(
                leagueService = leagueService,
                teamService = teamService,
                fixtureService = fixtureService,
                mockLeagueRepository = mockLeagueRepository,
                mockTeamRepository = mockTeamRepository,
                mockFixtureRepository = mockFixtureRepository,
                uidFactory = MockUidFactory(Clock.fixed(now, ZoneOffset.UTC)),
                clock = Clock.fixed(now, ZoneOffset.UTC),
            )
    }

    @Test
    fun `createScenario composes league team fixture resource services with one scenario uid`() {
        whenever(leagueService.createLeague(any())).thenAnswer {
            val command = it.arguments[0] as MockLeagueCreateCommand
            DomainResult.Success(
                LeagueModel(
                    uid = "league-1",
                    name = command.name,
                    photo = null,
                    available = command.available,
                ),
            )
        }
        whenever(teamService.createTeam(any())).thenAnswer {
            val command = it.arguments[0] as MockTeamCreateCommand
            DomainResult.Success(
                TeamModel(
                    uid = "${command.name}-uid",
                    name = command.name,
                    code = null,
                ),
            )
        }
        whenever(fixtureService.createFixture(any())).thenAnswer {
            val command = it.arguments[0] as MockFixtureCreateCommand
            val homeTeamCoreUid = requireNotNull(command.homeTeamCoreUid)
            val awayTeamCoreUid = requireNotNull(command.awayTeamCoreUid)
            DomainResult.Success(
                FixtureModel(
                    uid = "fixture-1",
                    leagueUid = command.leagueCoreUid,
                    schedule = FixtureModel.FixtureSchedule(kickoffAt = command.kickoff, round = ""),
                    homeTeam = FixtureModel.TeamSide(homeTeamCoreUid, homeTeamCoreUid, null),
                    awayTeam = FixtureModel.TeamSide(awayTeamCoreUid, awayTeamCoreUid, null),
                    status = FixtureModel.Status("Not Started", FixtureModel.StatusCode.NS, null, null),
                    score = FixtureModel.Score(null, null),
                    available = command.fixtureAvailable,
                ),
            )
        }

        val result =
            service.createScenario(
                MockSimpleFixtureScenarioCommand(
                    leagueName = "League",
                    homeTeamName = "Home",
                    awayTeamName = "Away",
                    scenarioName = "available lifecycle",
                ),
            )

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val value = (result as DomainResult.Success).value
        assertThat(value.scenarioUid).isNotBlank()
        assertThat(value.league.uid).isEqualTo("league-1")
        assertThat(value.homeTeam.uid).isEqualTo("Home-uid")
        assertThat(value.awayTeam.uid).isEqualTo("Away-uid")
        assertThat(value.fixture.uid).isEqualTo("fixture-1")
        assertThat(value.fixture.status.code).isEqualTo(FixtureModel.StatusCode.NS)
        assertThat(value.fixtureAvailable).isFalse()
    }
}
