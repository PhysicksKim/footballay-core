package com.footballay.core.web.admin.localization.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.common.result.DomainResult
import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.admin.localization.dto.CoreLocalizationResponse
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportContext
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportContextItem
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportEntityType
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportItem
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportResponse
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportValue
import com.footballay.core.web.admin.localization.ai.AiLocalizationContract
import com.footballay.core.web.admin.localization.dto.LocalizationResponse
import com.footballay.core.web.admin.localization.dto.SupportedLocaleResponse
import com.footballay.core.web.admin.localization.service.AdminLocalizationWebService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminLocalizationManagementControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @MockitoBean
    private lateinit var adminLocalizationWebService: AdminLocalizationWebService

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("supported locales endpoint는 code 목록을 반환한다")
    fun getSupportedLocales_returnsCodes() {
        given(adminLocalizationWebService.getSupportedLocales()).willReturn(listOf(SupportedLocaleResponse("en"), SupportedLocaleResponse("ko")))

        mockMvc
            .get("/api/v1/admin/localizations/locales")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].code") { value("en") }
                jsonPath("$[1].code") { value("ko") }
            }
    }

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("available League endpoint는 locale별 조립 응답을 반환한다")
    fun getAvailableLeagues_returnsLocalizedResponses() {
        given(adminLocalizationWebService.getAvailableLeagues(SupportedLocale.KO)).willReturn(
            DomainResult.Success(listOf(CoreLocalizationResponse("league-1", "League One", LocalizationResponse("리그 하나", null, false)))),
        )

        mockMvc
            .get("/api/v1/admin/localizations/leagues") { param("locale", "ko") }
            .andExpect {
                status { isOk() }
                jsonPath("$[0].uid") { value("league-1") }
                jsonPath("$[0].originalName") { value("League One") }
                jsonPath("$[0].localization.name") { value("리그 하나") }
            }
    }

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("PUT은 name과 shortName이 255자를 넘으면 validation 400을 반환한다")
    fun updateLocalization_returnsBadRequestForTooLongValue() {
        val request = mapOf("name" to "a".repeat(256))

        mockMvc
            .put("/api/v1/admin/localizations/leagues/{uid}/{locale}", "league-1", "ko") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
            status { isBadRequest() }
        }
    }

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("AI export endpoint는 Team context와 locale별 localization을 반환한다")
    fun exportForAi_returnsExportPayload() {
        val request = mapOf(
            "entityType" to "TEAM",
            "leagueUid" to "league-1",
            "locales" to listOf("en", "ko"),
            "uids" to listOf("team-1"),
        )
        given(adminLocalizationWebService.exportForAi(org.mockito.kotlin.any())).willReturn(
            DomainResult.Success(
                AiLocalizationExportResponse(
                    locales = listOf("en", "ko"),
                    entityType = AiLocalizationExportEntityType.TEAM,
                    context = AiLocalizationExportContext(AiLocalizationExportContextItem("league-1", "Premier League")),
                    items = listOf(AiLocalizationExportItem("team-1", "Arsenal", mapOf("en" to AiLocalizationExportValue("Arsenal", "ARS"), "ko" to AiLocalizationExportValue(null, null)))),
                ),
            ),
        )

        val result =
            mockMvc
                .post("/api/v1/admin/localizations/ai-export") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.version") { value(AiLocalizationContract.VERSION) }
                    jsonPath("$.entityType") { value("TEAM") }
                    jsonPath("$.context.league.uid") { value("league-1") }
                    jsonPath("$.context.team") { doesNotExist() }
                    jsonPath("$.items[0].localizations.en.shortName") { value("ARS") }
                }.andReturn()

        val missingLocalization = objectMapper.readTree(result.response.contentAsString).at("/items/0/localizations/ko")
        assertThat(missingLocalization.has("name")).isTrue()
        assertThat(missingLocalization.get("name").isNull).isTrue()
        assertThat(missingLocalization.has("shortName")).isTrue()
        assertThat(missingLocalization.get("shortName").isNull).isTrue()
    }
}
