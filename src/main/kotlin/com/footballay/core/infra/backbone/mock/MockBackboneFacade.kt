package com.footballay.core.infra.backbone.mock

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
import com.footballay.core.infra.backbone.mock.scenario.MockSimpleFixtureScenarioCommand
import com.footballay.core.infra.backbone.mock.scenario.MockSimpleFixtureScenarioResult
import com.footballay.core.infra.backbone.mock.scenario.MockSimpleFixtureScenarioService
import org.springframework.stereotype.Service

@Service
class MockBackboneFacade(
    private val leagueService: MockLeagueService,
    private val teamService: MockTeamService,
    private val fixtureService: MockFixtureService,
    private val scenarioService: MockSimpleFixtureScenarioService,
) {
    fun createLeague(command: MockLeagueCreateCommand): DomainResult<LeagueModel, DomainFail> =
        leagueService.createLeague(command)

    fun deleteLeague(leagueCoreUid: String): DomainResult<LeagueModel, DomainFail> =
        leagueService.deleteLeague(leagueCoreUid)

    fun createTeam(command: MockTeamCreateCommand): DomainResult<TeamModel, DomainFail> =
        teamService.createTeam(command)

    fun deleteTeam(teamCoreUid: String): DomainResult<TeamModel, DomainFail> =
        teamService.deleteTeam(teamCoreUid)

    fun createFixture(command: MockFixtureCreateCommand): DomainResult<FixtureModel, DomainFail> =
        fixtureService.createFixture(command)

    fun deleteFixture(fixtureCoreUid: String): DomainResult<FixtureModel, DomainFail> =
        fixtureService.deleteFixture(fixtureCoreUid)

    fun createSimpleFixtureScenario(command: MockSimpleFixtureScenarioCommand): DomainResult<MockSimpleFixtureScenarioResult, DomainFail> =
        scenarioService.createScenario(command)

    fun deleteSimpleFixtureScenarioByFixtureUid(fixtureCoreUid: String): DomainResult<MockSimpleFixtureScenarioResult, DomainFail> =
        scenarioService.deleteScenarioByFixtureUid(fixtureCoreUid)
}
