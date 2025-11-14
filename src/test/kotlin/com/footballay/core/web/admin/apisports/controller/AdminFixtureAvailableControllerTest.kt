package com.footballay.core.web.admin.apisports.controller

import com.footballay.core.TestSecurityConfig
import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.infra.facade.AvailableFixtureFacade
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * AdminFixtureController 단위 테스트
 *
 * Facade를 Mock으로 주입하여 Controller 레이어만 테스트합니다.
 */
@WebMvcTest(AdminFixtureAvailableController::class)
@Import(TestSecurityConfig::class)
class AdminFixtureAvailableControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var availableFixtureFacade: AvailableFixtureFacade

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `Fixture를 Available로 설정 성공`() {
        // Given
        val fixtureApiId = 1208021L
        val fixtureUid = "apisports:1208021"

        given(availableFixtureFacade.addAvailableFixture(fixtureApiId))
            .willReturn(DomainResult.Success(fixtureUid))

        // When & Then
        mockMvc
            .perform(
                put("/api/v1/admin/apisports/fixtures/$fixtureApiId/available")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{" + "\"available\": true" + "}"),
            ).andExpect(status().isOk)
            .andExpect(content().string(fixtureUid))

        verify(availableFixtureFacade).addAvailableFixture(fixtureApiId)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `존재하지 않는 Fixture - 404 반환`() {
        // Given
        val fixtureApiId = 999999L

        given(availableFixtureFacade.addAvailableFixture(fixtureApiId))
            .willReturn(
                DomainResult.Fail(
                    DomainFail.NotFound(
                        resource = "FIXTURE_API_SPORTS",
                        id = fixtureApiId.toString(),
                    ),
                ),
            )

        // When & Then
        mockMvc
            .perform(
                put("/api/v1/admin/apisports/fixtures/$fixtureApiId/available")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{" + "\"available\": true" + "}"),
            ).andExpect(status().isNotFound)
            .andExpect(content().string("Fixture not found: $fixtureApiId"))

        verify(availableFixtureFacade).addAvailableFixture(fixtureApiId)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `Job 등록 실패 - 400 반환`() {
        // Given
        val fixtureApiId = 1208021L

        given(availableFixtureFacade.addAvailableFixture(fixtureApiId))
            .willReturn(
                DomainResult.Fail(
                    DomainFail.Validation.single(
                        code = "PRE_MATCH_JOB_REGISTRATION_FAILED",
                        message = "Failed to register PreMatchJob for fixture apisports:1208021",
                        field = "fixtureApiId",
                    ),
                ),
            )

        // When & Then
        mockMvc
            .perform(
                put("/api/v1/admin/apisports/fixtures/$fixtureApiId/available")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{" + "\"available\": true" + "}"),
            ).andExpect(status().isBadRequest)
            .andExpect(content().string("Validation error: Failed to register PreMatchJob for fixture apisports:1208021"))

        verify(availableFixtureFacade).addAvailableFixture(fixtureApiId)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `kickoff 시간이 미정인 경우 - 400 반환`() {
        // Given
        val fixtureApiId = 1208021L

        given(availableFixtureFacade.addAvailableFixture(fixtureApiId))
            .willReturn(
                DomainResult.Fail(
                    DomainFail.Validation.single(
                        code = "KICKOFF_TIME_NOT_SET",
                        message = "경기 시작 시간이 미정입니다. 킥오프 시간 확정 후 다시 시도해주세요.",
                        field = "kickoff",
                    ),
                ),
            )

        // When & Then
        mockMvc
            .perform(
                put("/api/v1/admin/apisports/fixtures/$fixtureApiId/available")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{" + "\"available\": true" + "}"),
            ).andExpect(status().isBadRequest)
            .andExpect(content().string("Validation error: 경기 시작 시간이 미정입니다. 킥오프 시간 확정 후 다시 시도해주세요."))

        verify(availableFixtureFacade).addAvailableFixture(fixtureApiId)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `Fixture를 Unavailable로 설정 성공`() {
        // Given
        val fixtureApiId = 1208021L
        val fixtureUid = "apisports:1208021"

        given(availableFixtureFacade.removeAvailableFixture(fixtureApiId))
            .willReturn(DomainResult.Success(fixtureUid))

        // When & Then
        mockMvc
            .perform(
                put("/api/v1/admin/apisports/fixtures/$fixtureApiId/available")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{" + "\"available\": false" + "}"),
            ).andExpect(status().isOk)
            .andExpect(content().string(fixtureUid))

        verify(availableFixtureFacade).removeAvailableFixture(fixtureApiId)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `Unavailable 설정 시 존재하지 않는 Fixture - 404 반환`() {
        // Given
        val fixtureApiId = 999999L

        given(availableFixtureFacade.removeAvailableFixture(fixtureApiId))
            .willReturn(
                DomainResult.Fail(
                    DomainFail.NotFound(
                        resource = "FIXTURE_API_SPORTS",
                        id = fixtureApiId.toString(),
                    ),
                ),
            )

        // When & Then
        mockMvc
            .perform(
                put("/api/v1/admin/apisports/fixtures/$fixtureApiId/available")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{" + "\"available\": false" + "}"),
            ).andExpect(status().isNotFound)
            .andExpect(content().string("Fixture not found: $fixtureApiId"))

        verify(availableFixtureFacade).removeAvailableFixture(fixtureApiId)
    }
}
