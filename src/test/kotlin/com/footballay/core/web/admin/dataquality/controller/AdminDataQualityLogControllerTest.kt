package com.footballay.core.web.admin.dataquality.controller

import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import com.footballay.core.web.admin.dataquality.dto.AdminDataQualityLogDetailResponse
import com.footballay.core.web.admin.dataquality.dto.AdminDataQualityLogPageResponse
import com.footballay.core.web.admin.dataquality.dto.AdminDataQualityLogSummaryResponse
import com.footballay.core.web.admin.dataquality.service.AdminDataQualityLogQueryWebService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.Instant

@WebMvcTest(AdminDataQualityLogController::class)
@AutoConfigureMockMvc(addFilters = false)
class AdminDataQualityLogControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @MockitoBean
    private lateinit var queryWebService: AdminDataQualityLogQueryWebService

    @Test
    @DisplayName("log 목록 조회는 query parameter를 web service에 전달한다")
    fun getLogs_delegatesQueryParametersToWebService() {
        given(
            queryWebService.findLogs(
                provider = eq(FootballDataProvider.API_SPORTS),
                endpointKey = eq("fixture_single"),
                apiId = eq("1208397"),
                checkedAtFrom = eq(Instant.parse("2026-07-02T00:00:00Z")),
                checkedAtTo = eq(Instant.parse("2026-07-03T00:00:00Z")),
                hasIssue = eq(true),
                page = eq(1),
                size = eq(25),
            ),
        ).willReturn(
            AdminDataQualityLogPageResponse(
                content = listOf(summaryResponse()),
                page = 1,
                size = 25,
                totalElements = 1,
                totalPages = 1,
            ),
        )

        mockMvc
            .get("/api/v1/admin/data-quality/logs") {
                param("provider", "API_SPORTS")
                param("endpointKey", "fixture_single")
                param("apiId", "1208397")
                param("checkedAtFrom", "2026-07-02T00:00:00Z")
                param("checkedAtTo", "2026-07-03T00:00:00Z")
                param("hasIssue", "true")
                param("page", "1")
                param("size", "25")
            }.andExpect {
                status { isOk() }
                jsonPath("$.content[0].id") { value(1) }
                jsonPath("$.content[0].hasIssue") { value(true) }
                jsonPath("$.page") { value(1) }
                jsonPath("$.size") { value(25) }
            }

        verify(queryWebService).findLogs(
            provider = eq(FootballDataProvider.API_SPORTS),
            endpointKey = eq("fixture_single"),
            apiId = eq("1208397"),
            checkedAtFrom = eq(Instant.parse("2026-07-02T00:00:00Z")),
            checkedAtTo = eq(Instant.parse("2026-07-03T00:00:00Z")),
            hasIssue = eq(true),
            page = eq(1),
            size = eq(25),
        )
    }

    @Test
    @DisplayName("log 상세 조회는 web service 응답을 JSON으로 반환한다")
    fun getLog_returnsDetailResponse() {
        given(queryWebService.getLog(1L)).willReturn(detailResponse())

        mockMvc
            .get("/api/v1/admin/data-quality/logs/{id}", 1L)
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(1) }
                jsonPath("$.result.summary.issueCount") { value(2) }
                jsonPath("$.hasIssue") { value(true) }
            }

        verify(queryWebService).getLog(1L)
    }

    @Test
    @DisplayName("log 목록 조회는 page와 size 범위를 검증한다")
    fun getLogs_rejectsInvalidPageRequest() {
        mockMvc
            .get("/api/v1/admin/data-quality/logs") {
                param("page", "-1")
                param("size", "201")
            }.andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    @DisplayName("log 상세 조회는 양수가 아닌 id를 거부한다")
    fun getLog_rejectsNonPositiveId() {
        mockMvc
            .get("/api/v1/admin/data-quality/logs/{id}", 0L)
            .andExpect {
                status { isBadRequest() }
            }
    }

    private fun summaryResponse(): AdminDataQualityLogSummaryResponse =
        AdminDataQualityLogSummaryResponse(
            id = 1L,
            resultEventId = "result-1",
            rawEventId = "raw-1",
            provider = FootballDataProvider.API_SPORTS,
            endpointKey = "fixture_single",
            apiId = "1208397",
            canonicalHash = "sha256-result-1",
            rawJsonObjectKey = "data-quality/raw/api-sports/fixture_single/1208397/result-1.json.gz",
            scannerVersion = "rule-2026-07-02",
            checkedAt = Instant.parse("2026-07-02T08:00:00Z"),
            issueCount = 2,
            hasIssue = true,
            createdAt = Instant.parse("2026-07-02T08:01:00Z"),
        )

    private fun detailResponse(): AdminDataQualityLogDetailResponse =
        AdminDataQualityLogDetailResponse(
            id = 1L,
            resultEventId = "result-1",
            rawEventId = "raw-1",
            provider = FootballDataProvider.API_SPORTS,
            endpointKey = "fixture_single",
            apiId = "1208397",
            canonicalHash = "sha256-result-1",
            rawJsonObjectKey = "data-quality/raw/api-sports/fixture_single/1208397/result-1.json.gz",
            scannerVersion = "rule-2026-07-02",
            checkedAt = Instant.parse("2026-07-02T08:00:00Z"),
            issueCount = 2,
            hasIssue = true,
            result =
                (objectMapper.createObjectNode()).apply {
                    set<ObjectNode>(
                        "summary",
                        objectMapper.createObjectNode().put("issueCount", 2),
                    )
                },
            createdAt = Instant.parse("2026-07-02T08:01:00Z"),
            updatedAt = Instant.parse("2026-07-02T08:01:00Z"),
        )
}
