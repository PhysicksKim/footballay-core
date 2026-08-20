package com.footballay.core.web.admin.mockbackbone.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.common.result.map
import com.footballay.core.domain.model.FixtureModel
import com.footballay.core.domain.model.LeagueModel
import com.footballay.core.domain.model.TeamModel
import com.footballay.core.infra.backbone.mock.MockBackboneFacade
import com.footballay.core.infra.backbone.mock.resource.MockFixtureCreateCommand
import com.footballay.core.infra.backbone.mock.resource.MockLeagueCreateCommand
import com.footballay.core.infra.backbone.mock.resource.MockTeamCreateCommand
import com.footballay.core.infra.backbone.mock.scenario.MockSimpleFixtureScenarioCommand
import com.footballay.core.infra.backbone.mock.scenario.MockSimpleFixtureScenarioResult
import com.footballay.core.web.admin.mockbackbone.dto.MockFixtureCreateRequest
import com.footballay.core.web.admin.mockbackbone.dto.MockFixtureResponse
import com.footballay.core.web.admin.mockbackbone.dto.MockLeagueCreateRequest
import com.footballay.core.web.admin.mockbackbone.dto.MockLeagueResponse
import com.footballay.core.web.admin.mockbackbone.dto.MockSimpleFixtureScenarioCreateRequest
import com.footballay.core.web.admin.mockbackbone.dto.MockSimpleFixtureScenarioResponse
import com.footballay.core.web.admin.mockbackbone.dto.MockTeamCreateRequest
import com.footballay.core.web.admin.mockbackbone.dto.MockTeamResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class AdminMockBackboneWebService(
    private val mockBackboneFacade: MockBackboneFacade,
) {
    @PreAuthorize("hasRole('ADMIN')")
    fun createLeague(request: MockLeagueCreateRequest): DomainResult<MockLeagueResponse, DomainFail> =
        mockBackboneFacade
            .createLeague(
                MockLeagueCreateCommand(
                    name = request.name,
                    available = request.available,
                    scenarioName = request.scenarioName,
                ),
            ).map(::toResponse)

    @PreAuthorize("hasRole('ADMIN')")
    fun deleteLeague(leagueCoreUid: String): DomainResult<MockLeagueResponse, DomainFail> =
        mockBackboneFacade.deleteLeague(leagueCoreUid).map(::toResponse)

    @PreAuthorize("hasRole('ADMIN')")
    fun createTeam(request: MockTeamCreateRequest): DomainResult<MockTeamResponse, DomainFail> =
        mockBackboneFacade
            .createTeam(
                MockTeamCreateCommand(
                    name = request.name,
                    scenarioName = request.scenarioName,
                ),
            ).map(::toResponse)

    @PreAuthorize("hasRole('ADMIN')")
    fun deleteTeam(teamCoreUid: String): DomainResult<MockTeamResponse, DomainFail> =
        mockBackboneFacade.deleteTeam(teamCoreUid).map(::toResponse)

    @PreAuthorize("hasRole('ADMIN')")
    fun createFixture(request: MockFixtureCreateRequest): DomainResult<MockFixtureResponse, DomainFail> =
        mockBackboneFacade
            .createFixture(
                MockFixtureCreateCommand(
                    leagueCoreUid = request.leagueCoreUid,
                    homeTeamCoreUid = request.homeTeamCoreUid,
                    awayTeamCoreUid = request.awayTeamCoreUid,
                    kickoff = request.kickoff,
                    statusCode = request.statusCode,
                    fixtureAvailable = false,
                    scenarioName = request.scenarioName,
                ),
            ).map(::toResponse)

    @PreAuthorize("hasRole('ADMIN')")
    fun deleteFixture(fixtureCoreUid: String): DomainResult<MockFixtureResponse, DomainFail> =
        mockBackboneFacade.deleteFixture(fixtureCoreUid).map(::toResponse)

    @PreAuthorize("hasRole('ADMIN')")
    fun createSimpleFixtureScenario(
        request: MockSimpleFixtureScenarioCreateRequest,
    ): DomainResult<MockSimpleFixtureScenarioResponse, DomainFail> =
        mockBackboneFacade
            .createSimpleFixtureScenario(
                MockSimpleFixtureScenarioCommand(
                    leagueName = request.leagueName,
                    homeTeamName = request.homeTeamName,
                    awayTeamName = request.awayTeamName,
                    kickoffOffsetMinutes = request.kickoffOffsetMinutes,
                    statusCode = request.statusCode,
                    leagueAvailable = request.leagueAvailable,
                    scenarioName = request.scenarioName,
                ),
            ).map(::toResponse)

    @PreAuthorize("hasRole('ADMIN')")
    fun deleteSimpleFixtureScenarioByFixtureUid(
        fixtureCoreUid: String,
    ): DomainResult<MockSimpleFixtureScenarioResponse, DomainFail> =
        mockBackboneFacade.deleteSimpleFixtureScenarioByFixtureUid(fixtureCoreUid).map(::toResponse)

    private fun toResponse(model: LeagueModel): MockLeagueResponse =
        MockLeagueResponse(
            uid = model.uid,
            name = model.name,
            photo = model.photo,
            available = model.available,
        )

    private fun toResponse(model: TeamModel): MockTeamResponse =
        MockTeamResponse(
            uid = model.uid,
            name = model.name,
            code = model.code,
        )

    private fun toResponse(model: FixtureModel): MockFixtureResponse =
        MockFixtureResponse(
            uid = model.uid,
            leagueUid = model.leagueUid,
            kickoff = model.schedule.kickoffAt,
            round = model.schedule.round,
            homeTeam = model.homeTeam?.let(::toResponse),
            awayTeam = model.awayTeam?.let(::toResponse),
            statusText = model.status.statusText,
            statusCode = model.status.code.value,
            elapsed = model.status.elapsed,
            homeScore = model.score.home,
            awayScore = model.score.away,
            available = model.available,
        )

    private fun toResponse(team: FixtureModel.TeamSide): MockFixtureResponse.TeamSide =
        MockFixtureResponse.TeamSide(
            uid = team.uid,
            name = team.name,
            logo = team.logo,
        )

    private fun toResponse(result: MockSimpleFixtureScenarioResult): MockSimpleFixtureScenarioResponse =
        MockSimpleFixtureScenarioResponse(
            scenarioUid = result.scenarioUid,
            league = toResponse(result.league),
            homeTeam = toResponse(result.homeTeam),
            awayTeam = toResponse(result.awayTeam),
            fixture = toResponse(result.fixture),
        )
}
