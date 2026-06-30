package com.footballay.core.infra.backbone.mock.scenario

import com.footballay.core.common.result.DomainFail
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
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class MockSimpleFixtureScenarioService(
    private val leagueService: MockLeagueService,
    private val teamService: MockTeamService,
    private val fixtureService: MockFixtureService,
    private val mockLeagueRepository: MockBackboneLeagueRepository,
    private val mockTeamRepository: MockBackboneTeamRepository,
    private val mockFixtureRepository: MockBackboneFixtureRepository,
    private val uidFactory: MockUidFactory,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val clock: Clock = Clock.systemUTC(),
) {
    @Transactional
    fun createScenario(command: MockSimpleFixtureScenarioCommand): DomainResult<MockSimpleFixtureScenarioResult, DomainFail> {
        validate(command)?.let { return DomainResult.Fail(it) }

        val scenarioUid = uidFactory.scenarioUid()
        val kickoff = Instant.now(clock).plusSeconds(command.kickoffOffsetMinutes * 60)
        val league =
            leagueService
                .createLeague(
                    MockLeagueCreateCommand(
                        name = command.leagueName,
                        available = command.leagueAvailable,
                        scenarioUid = scenarioUid,
                        scenarioName = command.scenarioName,
                    ),
                ).getOrThrow()
        val homeTeam =
            teamService
                .createTeam(
                    MockTeamCreateCommand(
                        name = command.homeTeamName,
                        scenarioUid = scenarioUid,
                        scenarioName = command.scenarioName,
                    ),
                ).getOrThrow()
        val awayTeam =
            teamService
                .createTeam(
                    MockTeamCreateCommand(
                        name = command.awayTeamName,
                        scenarioUid = scenarioUid,
                        scenarioName = command.scenarioName,
                    ),
                ).getOrThrow()
        val fixture =
            fixtureService
                .createFixture(
                    MockFixtureCreateCommand(
                        leagueCoreUid = league.uid,
                        homeTeamCoreUid = homeTeam.uid,
                        awayTeamCoreUid = awayTeam.uid,
                        kickoff = kickoff,
                        statusCode = command.statusCode,
                        fixtureAvailable = false,
                        scenarioUid = scenarioUid,
                        scenarioName = command.scenarioName,
                    ),
                ).getOrThrow()

        return DomainResult.Success(
            MockSimpleFixtureScenarioResult(
                scenarioUid = scenarioUid,
                league = league,
                homeTeam = homeTeam,
                awayTeam = awayTeam,
                fixture = fixture,
            ),
        )
    }

    @Transactional
    fun deleteScenarioByFixtureUid(fixtureCoreUid: String): DomainResult<MockSimpleFixtureScenarioResult, DomainFail> {
        val mockFixture =
            mockFixtureRepository.findByFixtureCoreUid(fixtureCoreUid)
                ?: return DomainResult.Fail(DomainFail.NotFound("MOCK_BACKBONE_FIXTURE", fixtureCoreUid))
        val scenarioUid =
            mockFixture.scenarioUid
                ?: return DomainResult.Fail(
                    DomainFail.Validation.single(
                        code = "FIXTURE_HAS_NO_SCENARIO",
                        message = "해당 fixture 는 simple fixture scenario 로 생성된 데이터가 아닙니다.",
                        field = "fixtureCoreUid",
                    ),
                )

        val fixtures = mockFixtureRepository.findByScenarioUid(scenarioUid)
        val teams = mockTeamRepository.findByScenarioUid(scenarioUid)
        val leagues = mockLeagueRepository.findByScenarioUid(scenarioUid)
        val result =
            MockSimpleFixtureScenarioResult(
                scenarioUid = scenarioUid,
                league = leagueService.toModel(leagues.single()),
                homeTeam = teamService.toModel(teams[0]),
                awayTeam = teamService.toModel(teams[1]),
                fixture = fixtureService.toModel(fixtures.single()),
            )

        fixtures.forEach { fixtureService.deleteFixture(it.fixture.uid).getOrThrow() }
        teams.forEach { teamService.deleteTeam(it.team.uid).getOrThrow() }
        leagues.forEach { leagueService.deleteLeague(it.league.uid).getOrThrow() }

        return DomainResult.Success(result)
    }

    private fun validate(command: MockSimpleFixtureScenarioCommand): DomainFail.Validation? {
        val errors = mutableListOf<DomainFail.Validation.ValidationError>()
        if (command.leagueName.isBlank()) {
            errors += DomainFail.Validation.ValidationError("LEAGUE_NAME_BLANK", "리그 이름은 비어 있을 수 없습니다.", "leagueName")
        }
        if (command.homeTeamName.isBlank()) {
            errors += DomainFail.Validation.ValidationError("HOME_TEAM_NAME_BLANK", "홈팀 이름은 비어 있을 수 없습니다.", "homeTeamName")
        }
        if (command.awayTeamName.isBlank()) {
            errors += DomainFail.Validation.ValidationError("AWAY_TEAM_NAME_BLANK", "원정팀 이름은 비어 있을 수 없습니다.", "awayTeamName")
        }
        if (command.homeTeamName.trim() == command.awayTeamName.trim()) {
            errors += DomainFail.Validation.ValidationError("TEAMS_MUST_BE_DIFFERENT", "홈팀과 원정팀은 달라야 합니다.", "awayTeamName")
        }
        return if (errors.isEmpty()) null else DomainFail.Validation(errors)
    }

    private fun <T : Any> DomainResult<T, DomainFail>.getOrThrow(): T =
        when (this) {
            is DomainResult.Success -> value
            is DomainResult.Fail -> throw IllegalStateException("Unexpected mock resource creation failure: $error")
        }
}
