package com.footballay.core.web.admin.dataquality.controller

import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import com.footballay.core.infra.dataquality.result.model.DataQualityCheckStatus
import com.footballay.core.infra.dataquality.result.model.DataQualityMaxSeverity
import com.footballay.core.web.admin.dataquality.dto.AdminRawJsonDownloadUrlResponse
import com.footballay.core.web.admin.dataquality.dto.AdminQualityResultPageResponse
import com.footballay.core.web.admin.dataquality.service.AdminQualityResultQueryWebService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.Instant

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminQualityResultControllerTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockitoBean
    private lateinit var adminQualityResultQueryWebService: AdminQualityResultQueryWebService

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("quality result 목록 조회는 query parameter를 webservice에 전달한다")
    fun getResults_delegatesQueryParametersToWebService() {
        given(
            adminQualityResultQueryWebService.findResults(
                provider = eq(FootballDataProvider.API_SPORTS),
                endpointKey = eq("fixtureSingle"),
                checkedAtFrom = eq(Instant.parse("2026-07-07T00:00:00Z")),
                checkedAtTo = eq(Instant.parse("2026-07-08T00:00:00Z")),
                hasIssue = eq(true),
                maxSeverity = eq(DataQualityMaxSeverity.WARN),
                checkStatus = eq(DataQualityCheckStatus.NEED_CHECK),
                suggestedTypeCode = eq("EVENT_SUB_NO_PLAYER"),
                confirmedTypeCode = eq(null),
                archiveStatus = eq(null),
                parameterName = eq("fixtureId"),
                parameterValue = eq("1208397"),
                page = eq(0),
                size = eq(50),
            ),
        ).willReturn(emptyPage())

        mockMvc
            .get("/api/v1/admin/data-quality/results") {
                param("provider", "API_SPORTS")
                param("endpointKey", "fixtureSingle")
                param("checkedAtFrom", "2026-07-07T00:00:00Z")
                param("checkedAtTo", "2026-07-08T00:00:00Z")
                param("hasIssue", "true")
                param("maxSeverity", "WARN")
                param("checkStatus", "NEED_CHECK")
                param("suggestedTypeCode", "EVENT_SUB_NO_PLAYER")
                param("parameterName", "fixtureId")
                param("parameterValue", "1208397")
                param("page", "0")
                param("size", "50")
            }.andExpect {
                status { isOk() }
                jsonPath("$.content") { isArray() }
            }

        verify(adminQualityResultQueryWebService).findResults(
            provider = eq(FootballDataProvider.API_SPORTS),
            endpointKey = eq("fixtureSingle"),
            checkedAtFrom = eq(Instant.parse("2026-07-07T00:00:00Z")),
            checkedAtTo = eq(Instant.parse("2026-07-08T00:00:00Z")),
            hasIssue = eq(true),
            maxSeverity = eq(DataQualityMaxSeverity.WARN),
            checkStatus = eq(DataQualityCheckStatus.NEED_CHECK),
            suggestedTypeCode = eq("EVENT_SUB_NO_PLAYER"),
            confirmedTypeCode = eq(null),
            archiveStatus = eq(null),
            parameterName = eq("fixtureId"),
            parameterValue = eq("1208397"),
            page = eq(0),
            size = eq(50),
        )
    }

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("quality result 상세 조회는 resultId를 webservice에 전달한다")
    fun getResult_delegatesResultIdToWebService() {
        mockMvc
            .get("/api/v1/admin/data-quality/results/{resultId}", "result-1")
            .andExpect {
                status { isOk() }
            }

        verify(adminQualityResultQueryWebService).findResult("result-1")
    }

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("quality result raw json download url 조회는 resultId를 webservice에 전달한다")
    fun getRawJsonDownloadUrl_delegatesResultIdToWebService() {
        given(adminQualityResultQueryWebService.createRawJsonDownloadUrl("result-1"))
            .willReturn(
                AdminRawJsonDownloadUrlResponse(
                    downloadUrl = "file:///tmp/data-quality/raw/object.json.gz",
                    expiresAt = Instant.parse("2026-07-07T12:11:32Z"),
                ),
            )

        mockMvc
            .get("/api/v1/admin/data-quality/results/{resultId}/raw-json/download-url", "result-1")
            .andExpect {
                status { isOk() }
                jsonPath("$.downloadUrl") { value("file:///tmp/data-quality/raw/object.json.gz") }
                jsonPath("$.expiresAt") { value("2026-07-07T12:11:32Z") }
            }

        verify(adminQualityResultQueryWebService).createRawJsonDownloadUrl("result-1")
    }

    private fun emptyPage() =
        AdminQualityResultPageResponse(
            content = emptyList(),
            page = 0,
            size = 50,
            totalElements = 0,
            totalPages = 0,
        )
}
