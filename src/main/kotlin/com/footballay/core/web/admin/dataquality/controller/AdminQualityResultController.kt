package com.footballay.core.web.admin.dataquality.controller

import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import com.footballay.core.infra.dataquality.result.model.DataQualityArchiveStatus
import com.footballay.core.infra.dataquality.result.model.DataQualityCheckStatus
import com.footballay.core.infra.dataquality.result.model.DataQualityMaxSeverity
import com.footballay.core.web.admin.dataquality.dto.AdminRawJsonDownloadUrlResponse
import com.footballay.core.web.admin.dataquality.dto.AdminQualityResultDetailResponse
import com.footballay.core.web.admin.dataquality.dto.AdminQualityResultPageResponse
import com.footballay.core.web.admin.dataquality.service.AdminQualityResultQueryWebService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
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

@Tag(
    name = "Admin - Data Quality Results",
    description = "Data Quality MongoDB quality_results read-only Admin API",
)
@SecurityRequirement(name = "cookieAuth")
@Validated
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/data-quality/results")
class AdminQualityResultController(
    private val adminQualityResultQueryWebService: AdminQualityResultQueryWebService,
) {
    @Operation(summary = "Data Quality result 목록 조회")
    @ApiResponse(
        responseCode = "200",
        description = "Data Quality result page",
        content = [Content(schema = Schema(implementation = AdminQualityResultPageResponse::class))],
    )
    @GetMapping
    fun getResults(
        @RequestParam(required = false) provider: FootballDataProvider?,
        @RequestParam(required = false) endpointKey: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) checkedAtFrom: Instant?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) checkedAtTo: Instant?,
        @RequestParam(required = false) hasIssue: Boolean?,
        @RequestParam(required = false) maxSeverity: DataQualityMaxSeverity?,
        @RequestParam(required = false) checkStatus: DataQualityCheckStatus?,
        @RequestParam(required = false) suggestedTypeCode: String?,
        @RequestParam(required = false) confirmedTypeCode: String?,
        @RequestParam(required = false) archiveStatus: DataQualityArchiveStatus?,
        @Parameter(description = "parameters 배열의 단순 name 조건. parameterValue와 함께 사용하면 같은 element에 대해 elemMatch로 조회합니다.")
        @RequestParam(required = false) parameterName: String?,
        @Parameter(description = "parameters 배열의 단순 value 조건. parameterName 없이 사용할 수 없습니다.")
        @RequestParam(required = false) parameterValue: String?,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "50") size: Int,
    ): ResponseEntity<AdminQualityResultPageResponse> =
        ResponseEntity.ok(
            adminQualityResultQueryWebService.findResults(
                provider = provider,
                endpointKey = endpointKey,
                checkedAtFrom = checkedAtFrom,
                checkedAtTo = checkedAtTo,
                hasIssue = hasIssue,
                maxSeverity = maxSeverity,
                checkStatus = checkStatus,
                suggestedTypeCode = suggestedTypeCode,
                confirmedTypeCode = confirmedTypeCode,
                archiveStatus = archiveStatus,
                parameterName = parameterName,
                parameterValue = parameterValue,
                page = page,
                size = size,
            ),
        )

    @Operation(summary = "Data Quality result 상세 조회")
    @ApiResponse(
        responseCode = "200",
        description = "Data Quality result detail",
        content = [Content(schema = Schema(implementation = AdminQualityResultDetailResponse::class))],
    )
    @ApiResponse(responseCode = "404", description = "Data Quality result를 찾을 수 없음")
    @GetMapping("/{resultId}")
    fun getResult(
        @PathVariable resultId: String,
    ) = ResponseEntity.ok(adminQualityResultQueryWebService.findResult(resultId))

    @Operation(summary = "Data Quality raw JSON 다운로드 URL 발급")
    @ApiResponse(
        responseCode = "200",
        description = "Raw JSON download URL",
        content = [Content(schema = Schema(implementation = AdminRawJsonDownloadUrlResponse::class))],
    )
    @ApiResponse(responseCode = "404", description = "Data Quality result를 찾을 수 없음")
    @GetMapping("/{resultId}/raw-json/download-url")
    fun getRawJsonDownloadUrl(
        @PathVariable resultId: String,
    ) = ResponseEntity.ok(adminQualityResultQueryWebService.createRawJsonDownloadUrl(resultId))
}
