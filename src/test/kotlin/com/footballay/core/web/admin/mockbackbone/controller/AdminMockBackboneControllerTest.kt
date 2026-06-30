package com.footballay.core.web.admin.mockbackbone.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.fixture.FixtureStatusCode
import com.footballay.core.web.admin.mockbackbone.dto.MockFixtureCreateRequest
import com.footballay.core.web.admin.mockbackbone.dto.MockFixtureResponse
import com.footballay.core.web.admin.mockbackbone.dto.MockLeagueCreateRequest
import com.footballay.core.web.admin.mockbackbone.dto.MockLeagueResponse
import com.footballay.core.web.admin.mockbackbone.dto.MockSimpleFixtureScenarioCreateRequest
import com.footballay.core.web.admin.mockbackbone.dto.MockSimpleFixtureScenarioResponse
import com.footballay.core.web.admin.mockbackbone.dto.MockTeamResponse
import com.footballay.core.web.admin.mockbackbone.service.AdminMockBackboneWebService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import java.time.Instant

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = ["footballay.mock-backbone.admin-api.enabled=true"])
class AdminMockBackboneControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @MockitoBean
    private lateinit var adminMockBackboneWebService: AdminMockBackboneWebService

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("createLeague - 정상 요청이면 200과 mock 리그 응답을 반환한다")
    fun createLeague_success() {
        val request = MockLeagueCreateRequest(name = "Mock League", available = true, scenarioName = "scenario-a")
        val response =
            MockLeagueResponse(
                uid = "league-core-1",
                name = "Mock League",
                nameKo = null,
                photo = null,
                available = true,
            )

        given(adminMockBackboneWebService.createLeague(request))
            .willReturn(DomainResult.Success(response))

        mockMvc
            .post("/api/v1/admin/mock/leagues") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
                jsonPath("$.uid") { value("league-core-1") }
                jsonPath("$.name") { value("Mock League") }
                jsonPath("$.available") { value(true) }
            }
    }

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("createFixture - nullable team/kickoff 요청을 허용하고 200과 mock 경기 응답을 반환한다")
    fun createFixture_nullableFields_success() {
        val request =
            MockFixtureCreateRequest(
                leagueCoreUid = "league-core-1",
                homeTeamCoreUid = null,
                awayTeamCoreUid = null,
                kickoff = null,
                statusCode = FixtureStatusCode.NS,
                scenarioName = "fixture-without-schedule",
            )
        val response =
            MockFixtureResponse(
                uid = "fixture-core-1",
                leagueUid = "league-core-1",
                kickoff = null,
                round = "Mock Round",
                homeTeam = null,
                awayTeam = null,
                statusText = "Not Started",
                statusCode = "NS",
                elapsed = null,
                homeScore = null,
                awayScore = null,
                available = false,
            )

        given(adminMockBackboneWebService.createFixture(request))
            .willReturn(DomainResult.Success(response))

        mockMvc
            .post("/api/v1/admin/mock/fixtures") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
                jsonPath("$.uid") { value("fixture-core-1") }
                jsonPath("$.leagueUid") { value("league-core-1") }
                jsonPath("$.homeTeam") { doesNotExist() }
                jsonPath("$.awayTeam") { doesNotExist() }
                jsonPath("$.available") { value(false) }
            }
    }

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("createSimpleFixtureScenario - 정상 요청이면 200과 scenario 응답을 반환한다")
    fun createSimpleFixtureScenario_success() {
        val request =
            MockSimpleFixtureScenarioCreateRequest(
                leagueName = "Mock League",
                homeTeamName = "Mock Home",
                awayTeamName = "Mock Away",
                kickoffOffsetMinutes = 30,
                statusCode = FixtureStatusCode.NS,
                leagueAvailable = true,
                scenarioName = "simple-scenario",
            )
        val response =
            MockSimpleFixtureScenarioResponse(
                scenarioUid = "scenario-1",
                league = mockLeagueResponse(),
                homeTeam = mockTeamResponse(uid = "home-team-1", name = "Mock Home"),
                awayTeam = mockTeamResponse(uid = "away-team-1", name = "Mock Away"),
                fixture = mockFixtureResponse(),
            )

        given(adminMockBackboneWebService.createSimpleFixtureScenario(request))
            .willReturn(DomainResult.Success(response))

        mockMvc
            .post("/api/v1/admin/mock/scenarios/simple-fixture") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
                jsonPath("$.scenarioUid") { value("scenario-1") }
                jsonPath("$.league.uid") { value("league-core-1") }
                jsonPath("$.homeTeam.uid") { value("home-team-1") }
                jsonPath("$.awayTeam.uid") { value("away-team-1") }
                jsonPath("$.fixture.uid") { value("fixture-core-1") }
            }
    }

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("deleteFixture - 정상 요청이면 200과 삭제된 mock 경기 응답을 반환한다")
    fun deleteFixture_success() {
        val response = mockFixtureResponse()

        given(adminMockBackboneWebService.deleteFixture("fixture-core-1"))
            .willReturn(DomainResult.Success(response))

        mockMvc
            .delete("/api/v1/admin/mock/fixtures/{fixtureCoreUid}", "fixture-core-1")
            .andExpect {
                status { isOk() }
                jsonPath("$.uid") { value("fixture-core-1") }
                jsonPath("$.available") { value(false) }
            }
    }

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("deleteFixture - 대상이 없으면 404를 반환한다")
    fun deleteFixture_notFound_returns404() {
        given(adminMockBackboneWebService.deleteFixture("missing-fixture"))
            .willReturn(DomainResult.Fail(DomainFail.NotFound(resource = "MockBackboneFixture", id = "missing-fixture")))

        mockMvc
            .delete("/api/v1/admin/mock/fixtures/{fixtureCoreUid}", "missing-fixture")
            .andExpect {
                status { isNotFound() }
            }
    }

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("createLeague - name이 blank이면 400 + WEB_VALIDATION_ERROR를 반환한다")
    fun createLeague_blankName_returns400() {
        val request = MockLeagueCreateRequest(name = " ")

        mockMvc
            .post("/api/v1/admin/mock/leagues") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("WEB_VALIDATION_ERROR") }
                jsonPath("$.errors") { isArray() }
            }
    }

    private fun mockLeagueResponse(): MockLeagueResponse =
        MockLeagueResponse(
            uid = "league-core-1",
            name = "Mock League",
            nameKo = null,
            photo = null,
            available = true,
        )

    private fun mockTeamResponse(
        uid: String,
        name: String,
    ): MockTeamResponse =
        MockTeamResponse(
            uid = uid,
            name = name,
            nameKo = null,
            code = null,
        )

    private fun mockFixtureResponse(): MockFixtureResponse =
        MockFixtureResponse(
            uid = "fixture-core-1",
            leagueUid = "league-core-1",
            kickoff = Instant.parse("2026-05-30T12:00:00Z"),
            round = "Mock Round",
            homeTeam =
                MockFixtureResponse.TeamSide(
                    uid = "home-team-1",
                    name = "Mock Home",
                    nameKo = null,
                    logo = null,
                ),
            awayTeam =
                MockFixtureResponse.TeamSide(
                    uid = "away-team-1",
                    name = "Mock Away",
                    nameKo = null,
                    logo = null,
                ),
            statusText = "Not Started",
            statusCode = "NS",
            elapsed = null,
            homeScore = null,
            awayScore = null,
            available = false,
        )
}
