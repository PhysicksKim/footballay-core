package com.footballay.core.web.admin.mockbackbone

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.domain.fixture.FixtureStatusCode
import com.footballay.core.web.admin.apisports.dto.AvailabilityToggleRequest
import com.footballay.core.web.admin.mockbackbone.dto.MockSimpleFixtureScenarioCreateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = ["footballay.mock-backbone.admin-api.enabled=true"])
class AdminMockBackboneLifecycleSmokeTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("MockBackbone scenario는 Core available API를 통해 Quartz available job lifecycle로 합류한다")
    fun mockScenario_runsThroughCoreAvailableQuartzLifecycle() {
        val scenario = createScenario()
        val leagueUid = scenario["league"]["uid"].asText()
        val fixtureUid = scenario["fixture"]["uid"].asText()

        setLeagueAvailable(leagueUid, true)
        setFixtureAvailable(fixtureUid, true)

        assertAvailableJobs(
            leagueUid = leagueUid,
            fixtureUid = fixtureUid,
            expectedPhases = setOf("PRE", "LIVE"),
        )

        setFixtureAvailable(fixtureUid, false)

        assertAvailableJobs(
            leagueUid = leagueUid,
            fixtureUid = fixtureUid,
            expectedPhases = emptySet(),
        )

        deleteScenario(fixtureUid)
    }

    private fun createScenario(): com.fasterxml.jackson.databind.JsonNode {
        val request =
            MockSimpleFixtureScenarioCreateRequest(
                leagueName = "Smoke Mock League",
                homeTeamName = "Smoke Home",
                awayTeamName = "Smoke Away",
                kickoffOffsetMinutes = 120,
                statusCode = FixtureStatusCode.NS,
                leagueAvailable = false,
                scenarioName = "available-lifecycle-smoke",
            )

        val result =
            mockMvc
                .post("/api/v1/admin/mock/scenarios/simple-fixture") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isOk() }
                }.andReturn()

        return objectMapper.readTree(result.response.contentAsString)
    }

    private fun setLeagueAvailable(
        leagueUid: String,
        available: Boolean,
    ) {
        mockMvc
            .put("/api/v1/admin/leagues/{leagueCoreUid}/available", leagueUid) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(AvailabilityToggleRequest(available = available))
            }.andExpect {
                status { isOk() }
                jsonPath("$.uid") { value(leagueUid) }
                jsonPath("$.available") { value(available) }
            }
    }

    private fun setFixtureAvailable(
        fixtureUid: String,
        available: Boolean,
    ) {
        mockMvc
            .put("/api/v1/admin/fixtures/{fixtureCoreUid}/available", fixtureUid) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(AvailabilityToggleRequest(available = available))
            }.andExpect {
                status { isOk() }
                jsonPath("$.uid") { value(fixtureUid) }
                jsonPath("$.available") { value(available) }
            }
    }

    private fun assertAvailableJobs(
        leagueUid: String,
        fixtureUid: String,
        expectedPhases: Set<String>,
    ) {
        val result =
            mockMvc
                .get("/api/v1/admin/quartz/jobs") {
                    param("groupPrefix", "league:match:$leagueUid")
                }.andExpect {
                    status { isOk() }
                }.andReturn()

        val jobs = objectMapper.readTree(result.response.contentAsString)
        val actualPhases =
            jobs
                .filter { node ->
                    node["parsedIdentity"]?.get("owner")?.asText() == "AVAILABLE" &&
                        node["parsedIdentity"]?.get("fixtureUid")?.asText() == fixtureUid
                }.map { node -> node["parsedIdentity"]["phase"].asText() }
                .toSet()

        assertThat(actualPhases).isEqualTo(expectedPhases)
    }

    private fun deleteScenario(fixtureUid: String) {
        mockMvc
            .delete("/api/v1/admin/mock/scenarios/simple-fixture/by-fixture/{fixtureCoreUid}", fixtureUid)
            .andExpect {
                status { isOk() }
            }
    }
}
