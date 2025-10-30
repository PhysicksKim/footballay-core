package com.footballay.core.web.admin.apisports.controller

import com.footballay.core.TestSecurityConfig
import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.logger
import com.footballay.core.web.admin.apisports.dto.LeaguesSyncResultDto
import com.footballay.core.web.admin.apisports.service.AdminApiSportsWebService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
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

@Disabled("AdminApiSportsWebService 구현 후 활성화 필요")
@WebMvcTest(AdminApiSportsController::class)
@Import(TestSecurityConfig::class)
class AdminApiSportsControllerTest {
    @Autowired
    private lateinit var context: WebApplicationContext

    @MockitoBean
    private lateinit var adminApiSportsWebService: AdminApiSportsWebService

    private lateinit var mvc: MockMvc

    val log = logger()

    @BeforeEach
    fun setup() {
        mvc =
            MockMvcBuilders
                .webAppContextSetup(context)
                .apply<DefaultMockMvcBuilder>(springSecurity())
                .build()
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `컨트롤러 헬스 테스트 엔드포인트가 정상 동작한다`() {
        mvc
            .get("/api/v1/admin/apisports/test")
            .andExpect {
                status { isOk() }
                content { contentTypeCompatibleWith(MediaType.TEXT_PLAIN) }
            }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `현재 시즌 리그 동기화가 성공한다`() {
        // Given
        val mockResult = LeaguesSyncResultDto(syncedCount = 20)
        `when`(adminApiSportsWebService.syncCurrentLeagues())
            .thenReturn(DomainResult.Success(mockResult))

        // When & Then
        mvc
            .post("/api/v1/admin/apisports/leagues/sync") {
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `특정 리그의 Fixture 동기화가 성공한다`() {
        // Given
        val leagueId = 39L
        val syncedCount = 380
        `when`(adminApiSportsWebService.syncFixturesOfLeague(leagueId))
            .thenReturn(DomainResult.Success(syncedCount))

        // When & Then
        mvc
            .post("/api/v1/admin/apisports/leagues/$leagueId/fixtures/sync") {
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isOk() }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    jsonPath("$") { value(syncedCount) }
                }
            }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `존재하지 않는 리그로 Fixture 동기화 시 404를 반환한다`() {
        // Given
        val leagueId = 99999L
        `when`(adminApiSportsWebService.syncFixturesOfLeague(leagueId))
            .thenReturn(
                DomainResult.Fail(
                    DomainFail.NotFound(
                        resource = "LEAGUE",
                        id = leagueId.toString(),
                    ),
                ),
            )

        // When & Then
        mvc
            .post("/api/v1/admin/apisports/leagues/$leagueId/fixtures/sync") {
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `리그의 현재 시즌이 설정되지 않은 경우 400을 반환한다`() {
        // Given
        val leagueId = 39L
        `when`(adminApiSportsWebService.syncFixturesOfLeague(leagueId))
            .thenReturn(
                DomainResult.Fail(
                    DomainFail.Validation.single(
                        code = "CURRENT_SEASON_NOT_SET",
                        message = "Current season is not set for league",
                        field = "season",
                    ),
                ),
            )

        // When & Then
        mvc
            .post("/api/v1/admin/apisports/leagues/$leagueId/fixtures/sync") {
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `리그를 Available로 설정 성공`() {
        // Given
        val leagueId = 39L
        val leagueUid = "league_uid_123"
        `when`(adminApiSportsWebService.setLeagueAvailable(leagueId, true))
            .thenReturn(DomainResult.Success(leagueUid))

        // When & Then
        mvc
            .put("/api/v1/admin/apisports/leagues/$leagueId/available") {
                contentType = MediaType.APPLICATION_JSON
                content = "{" + "\"available\": true" + "}"
            }.andExpect {
                status { isOk() }
                content {
                    string(leagueUid)
                }
            }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `리그를 Unavailable로 설정 성공`() {
        // Given
        val leagueId = 39L
        val leagueUid = "league_uid_123"
        `when`(adminApiSportsWebService.setLeagueAvailable(leagueId, false))
            .thenReturn(DomainResult.Success(leagueUid))

        // When & Then
        mvc
            .put("/api/v1/admin/apisports/leagues/$leagueId/available") {
                contentType = MediaType.APPLICATION_JSON
                content = "{" + "\"available\": false" + "}"
            }.andExpect {
                status { isOk() }
                content {
                    string(leagueUid)
                }
            }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `존재하지 않는 리그로 Available 설정 시 404를 반환한다`() {
        // Given
        val leagueId = 99999L
        `when`(adminApiSportsWebService.setLeagueAvailable(leagueId, true))
            .thenReturn(
                DomainResult.Fail(
                    DomainFail.NotFound(
                        resource = "LEAGUE_CORE",
                        id = leagueId.toString(),
                    ),
                ),
            )

        // When & Then
        mvc
            .put("/api/v1/admin/apisports/leagues/$leagueId/available") {
                contentType = MediaType.APPLICATION_JSON
                content = "{" + "\"available\": true" + "}"
            }.andExpect {
                status { isNotFound() }
            }
    }
}
