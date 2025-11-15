package com.footballay.core.web.admin.apisports.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.TestSecurityConfig
import com.footballay.core.common.result.DomainResult
import com.footballay.core.web.admin.apisports.dto.ToggleAvailableResponse
import com.footballay.core.web.admin.apisports.service.AdminFixtureAvailableWebService
import com.footballay.core.web.admin.common.dto.AvailabilityToggleRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.put
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
class AdminFixtureAvailableControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @MockitoBean
    private lateinit var adminFixtureAvailableWebService: AdminFixtureAvailableWebService

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("setFixtureAvailable - 정상 요청이면 200과 UID 문자열을 반환한다")
    fun setFixtureAvailable_success() {
        // given
        val fixtureApiId = 1208021L
        val uid = "fixture-uid-123"

        given(adminFixtureAvailableWebService.setFixtureAvailable(fixtureApiId, true))
            .willReturn(DomainResult.Success(ToggleAvailableResponse(uid = uid, available = true)))

        val body = AvailabilityToggleRequest(available = true)
        val json = objectMapper.writeValueAsString(body)

        // when & then
        mockMvc
            .put("/api/v1/admin/apisports/fixtures/{fixtureApiId}/available", fixtureApiId) {
                contentType = MediaType.APPLICATION_JSON
                content = json
            }.andExpect {
                status { isOk() }
                // String 이라 JSON `"uid"` 로 내려가므로 그냥 문자열 비교
                jsonPath("$.uid") { value(uid) }
                jsonPath("$.available") { value(true) }
            }
    }

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("setFixtureAvailable - fixtureApiId가 음수면 Bean Validation으로 400 + WEB_VALIDATION_ERROR를 반환한다")
    fun setFixtureAvailable_negativeId_returns400() {
        val body = AvailabilityToggleRequest(available = true)
        val json = objectMapper.writeValueAsString(body)

        mockMvc
            .put("/api/v1/admin/apisports/fixtures/{fixtureApiId}/available", -1L) {
                contentType = MediaType.APPLICATION_JSON
                content = json
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("WEB_VALIDATION_ERROR") }
                jsonPath("$.errors") { isArray() }
            }
    }
}
