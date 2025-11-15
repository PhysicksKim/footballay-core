package com.footballay.core.web.admin.apisports.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.TestSecurityConfig
import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.logger
import com.footballay.core.web.admin.apisports.dto.LeagueSeasonRequest
import com.footballay.core.web.admin.apisports.dto.LeaguesSyncResultDto
import com.footballay.core.web.admin.apisports.dto.TeamsSyncResultDto
import com.footballay.core.web.admin.apisports.service.AdminApiSportsQueryWebService
import com.footballay.core.web.admin.apisports.service.AdminApiSportsWebService
import com.footballay.core.web.admin.apisports.service.AdminFixtureQueryWebService
import com.footballay.core.web.admin.apisports.service.AdminLeagueQueryWebService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(AdminApiSportsController::class)
@Import(TestSecurityConfig::class)
class AdminApiSportsControllerTest(
    @Autowired private val objectMapper: ObjectMapper,
) {
    @Autowired
    private lateinit var context: WebApplicationContext

    @MockitoBean
    private lateinit var adminApiSportsWebService: AdminApiSportsWebService

    @MockitoBean
    private lateinit var adminLeagueQueryWebService: AdminLeagueQueryWebService

    @MockitoBean
    private lateinit var adminFixtureQueryWebService: AdminFixtureQueryWebService

    @MockitoBean
    private lateinit var adminApiSportsQueryWebService: AdminApiSportsQueryWebService

    private lateinit var mockMvc: MockMvc

    val log = logger()

    @BeforeEach
    fun setup() {
        mockMvc =
            MockMvcBuilders
                .webAppContextSetup(context)
                .apply<DefaultMockMvcBuilder>(springSecurity())
                .build()
    }

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("syncCurrentLeagues - DomainResult.Success이면 200과 DTO를 반환한다")
    fun syncCurrentLeagues_success() {
        // given
        val dto =
            LeaguesSyncResultDto(
                syncedCount = 10,
                message = "현재 시즌 리그 10 개가 동기화되었습니다",
            )
        given(adminApiSportsWebService.syncCurrentLeagues())
            .willReturn(DomainResult.Success(dto))

        // when & then
        mockMvc
            .post("/api/v1/admin/apisports/leagues/sync") {
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                jsonPath("$.syncedCount") { value(10) }
                jsonPath("$.message") { value("현재 시즌 리그 10 개가 동기화되었습니다") }
            }
    }

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("syncTeamsOfLeague - leagueId가 음수이면 Bean Validation이 400 + WEB_VALIDATION_ERROR를 반환한다")
    fun syncTeamsOfLeague_negativeLeagueId_returns400() {
        val body = LeagueSeasonRequest(season = 2024)

        mockMvc
            .post("/api/v1/admin/apisports/leagues/{leagueId}/teams/sync", -1L) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(body)
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("WEB_VALIDATION_ERROR") }
                jsonPath("$.errors") { isArray() }
            }
    }

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("getLeagueFixtures - mode가 허용값이 아니면 400 + WEB_VALIDATION_ERROR를 반환한다")
    fun getLeagueFixtures_invalidMode_returns400() {
        mockMvc
            .get("/api/v1/admin/apisports/leagues/{leagueId}/fixtures", 1L) {
                param("mode", "something-else") // exact|nearest 아님
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("WEB_VALIDATION_ERROR") }
                jsonPath("$.errors") { isArray() }
            }
    }

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("syncTeamsOfLeague - 정상 입력이면 200과 TeamsSyncResultDto를 반환한다")
    fun syncTeamsOfLeague_success() {
        // given
        val leagueId = 39L
        val dto =
            TeamsSyncResultDto(
                syncedCount = 20,
                leagueApiId = leagueId,
                season = 2024,
                message = "리그(39)의 팀 20 개가 동기화되었습니다",
            )

        given(adminApiSportsWebService.syncTeamsOfLeague(leagueId, 2024))
            .willReturn(DomainResult.Success(dto))

        val body = LeagueSeasonRequest(season = 2024)
        val json = objectMapper.writeValueAsString(body)

        // when & then
        mockMvc
            .post("/api/v1/admin/apisports/leagues/{leagueId}/teams/sync", leagueId) {
                contentType = MediaType.APPLICATION_JSON
                content = json
            }.andExpect {
                status { isOk() }
                jsonPath("$.syncedCount") { value(20) }
                jsonPath("$.leagueApiId") { value(39) }
                jsonPath("$.season") { value(2024) }
            }
    }
}
