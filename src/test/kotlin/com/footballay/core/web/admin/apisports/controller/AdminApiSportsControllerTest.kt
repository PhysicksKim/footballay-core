package com.footballay.core.web.admin.apisports.controller

import com.footballay.core.TestSecurityConfig
import com.footballay.core.logger
import com.footballay.core.web.admin.apisports.dto.LeaguesSyncResultDto
import com.footballay.core.web.admin.apisports.service.AdminApiSportsWebService
import com.footballay.core.common.result.DomainResult
import org.junit.jupiter.api.BeforeEach
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
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

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
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `컨트롤러 헬스 테스트 엔드포인트가 정상 동작한다`() {
        mvc.get("/api/v1/admin/apisports/test")
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
        mvc.post("/api/v1/admin/apisports/leagues/sync") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }



}


