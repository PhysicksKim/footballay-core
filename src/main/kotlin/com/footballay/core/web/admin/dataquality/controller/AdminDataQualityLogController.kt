package com.footballay.core.web.admin.dataquality.controller

import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import com.footballay.core.web.admin.dataquality.dto.AdminDataQualityLogDetailResponse
import com.footballay.core.web.admin.dataquality.dto.AdminDataQualityLogPageResponse
import com.footballay.core.web.admin.dataquality.service.AdminDataQualityLogQueryWebService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@Tag(name = "Admin - Data Quality", description = "Data Quality result log 조회용 Admin API")
@SecurityRequirement(name = "cookieAuth")
@Validated
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/data-quality/logs")
class AdminDataQualityLogController(
    private val queryWebService: AdminDataQualityLogQueryWebService,
) {
    @Operation(summary = "Data Quality result log 목록 조회")
    @GetMapping
    fun getLogs(
        @Parameter(description = "Provider filter", example = "API_SPORTS")
        @RequestParam(required = false) provider: FootballDataProvider?,
        @Parameter(description = "Endpoint key filter", example = "fixture_single")
        @RequestParam(required = false) endpointKey: String?,
        @Parameter(description = "Provider API id filter", example = "1208397")
        @RequestParam(required = false) apiId: String?,
        @Parameter(description = "checkedAt inclusive lower bound", example = "2026-07-02T00:00:00Z")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        checkedAtFrom: Instant?,
        @Parameter(description = "checkedAt inclusive upper bound", example = "2026-07-03T00:00:00Z")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        checkedAtTo: Instant?,
        @Parameter(description = "true 이면 issueCount > 0, false 이면 issueCount = 0", example = "true")
        @RequestParam(required = false) hasIssue: Boolean?,
        @Parameter(description = "0-based page", example = "0")
        @RequestParam(required = false, defaultValue = "0")
        @Min(0)
        page: Int,
        @Parameter(description = "page size", example = "50")
        @RequestParam(required = false, defaultValue = "50")
        @Min(1)
        @Max(200)
        size: Int,
    ): ResponseEntity<AdminDataQualityLogPageResponse> =
        ResponseEntity.ok(
            queryWebService.findLogs(
                provider = provider,
                endpointKey = endpointKey,
                apiId = apiId,
                checkedAtFrom = checkedAtFrom,
                checkedAtTo = checkedAtTo,
                hasIssue = hasIssue,
                page = page,
                size = size,
            ),
        )

    @Operation(summary = "Data Quality result log 상세 조회")
    @GetMapping("/{id}")
    fun getLog(
        @Parameter(description = "Data quality result log id", example = "1")
        @PathVariable
        @Positive
        id: Long,
    ): ResponseEntity<AdminDataQualityLogDetailResponse> = ResponseEntity.ok(queryWebService.getLog(id))
}
