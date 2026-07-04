package com.footballay.core.web.admin.dataquality.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.domain.dataquality.AdminDataQualityLogDetailModel
import com.footballay.core.domain.dataquality.AdminDataQualityLogQueryFacade
import com.footballay.core.domain.dataquality.AdminDataQualityLogQueryFilter
import com.footballay.core.domain.dataquality.AdminDataQualityLogSummaryModel
import com.footballay.core.domain.dataquality.AdminDataQualityRawJsonDownloadUrlModel
import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import com.footballay.core.web.admin.dataquality.dto.AdminDataQualityLogDetailResponse
import com.footballay.core.web.admin.dataquality.dto.AdminDataQualityLogPageResponse
import com.footballay.core.web.admin.dataquality.dto.AdminDataQualityRawJsonDownloadUrlResponse
import com.footballay.core.web.admin.dataquality.dto.AdminDataQualityLogSummaryResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

/**
 * Data Quality 로그 조회 결과를 Admin API 응답 DTO로 조립하는 웹 서비스입니다.
 */
@Service
class AdminDataQualityLogQueryWebService(
    private val queryFacade: AdminDataQualityLogQueryFacade,
    private val objectMapper: ObjectMapper,
) {
    @PreAuthorize("hasRole('ADMIN')")
    fun findLogs(
        provider: FootballDataProvider?,
        endpointKey: String?,
        apiId: String?,
        checkedAtFrom: Instant?,
        checkedAtTo: Instant?,
        hasIssue: Boolean?,
        page: Int,
        size: Int,
    ): AdminDataQualityLogPageResponse {
        validateFindLogsRequest(
            endpointKey = endpointKey,
            apiId = apiId,
            checkedAtFrom = checkedAtFrom,
            checkedAtTo = checkedAtTo,
            page = page,
            size = size,
        )

        val pageable = pageOf(page, size)
        val result =
            queryFacade.findLogs(
                filter =
                    AdminDataQualityLogQueryFilter(
                        provider = provider,
                        endpointKey = endpointKey,
                        apiId = apiId,
                        checkedAtFrom = checkedAtFrom,
                        checkedAtTo = checkedAtTo,
                        hasIssue = hasIssue,
                    ),
                pageable = pageable,
            )

        return AdminDataQualityLogPageResponse(
            content = result.content.map(::toSummaryResponse),
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }

    @PreAuthorize("hasRole('ADMIN')")
    fun getLog(id: Long): AdminDataQualityLogDetailResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }

        val log =
            try {
                queryFacade.getLog(id)
            } catch (e: NoSuchElementException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message, e)
            }

        return toDetailResponse(log)
    }

    @PreAuthorize("hasRole('ADMIN')")
    fun createRawJsonDownloadUrl(id: Long): AdminDataQualityRawJsonDownloadUrlResponse {
        if (id <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive")
        }

        val downloadUrl =
            try {
                queryFacade.createRawJsonDownloadUrl(id)
            } catch (e: NoSuchElementException) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message, e)
            } catch (e: RuntimeException) {
                throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to create raw JSON download URL", e)
            }

        return toRawJsonDownloadUrlResponse(downloadUrl)
    }

    private fun pageOf(
        page: Int,
        size: Int,
    ): PageRequest =
        PageRequest.of(
            page.coerceAtLeast(0),
            size.coerceIn(1, 200),
            Sort.by(
                Sort.Order.desc("checkedAt"),
                Sort.Order.desc("id"),
            ),
        )

    private fun validateFindLogsRequest(
        endpointKey: String?,
        apiId: String?,
        checkedAtFrom: Instant?,
        checkedAtTo: Instant?,
        page: Int,
        size: Int,
    ) {
        if (endpointKey != null && endpointKey.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "endpointKey must not be blank")
        }
        if (apiId != null && apiId.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "apiId must not be blank")
        }
        if (checkedAtFrom != null && checkedAtTo != null && checkedAtFrom.isAfter(checkedAtTo)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "checkedAtFrom must be before or equal to checkedAtTo")
        }
        if (page < 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be greater than or equal to 0")
        }
        if (size !in 1..200) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be between 1 and 200")
        }
    }

    private fun toSummaryResponse(log: AdminDataQualityLogSummaryModel): AdminDataQualityLogSummaryResponse =
        AdminDataQualityLogSummaryResponse(
            id = log.id,
            resultEventId = log.resultEventId,
            rawEventId = log.rawEventId,
            provider = log.provider,
            endpointKey = log.endpointKey,
            apiId = log.apiId,
            canonicalHash = log.canonicalHash,
            rawJsonObjectKey = log.rawJsonObjectKey,
            scannerVersion = log.scannerVersion,
            checkedAt = log.checkedAt,
            issueCount = log.issueCount,
            hasIssue = log.hasIssue,
            createdAt = log.createdAt,
        )

    private fun toDetailResponse(log: AdminDataQualityLogDetailModel): AdminDataQualityLogDetailResponse =
        AdminDataQualityLogDetailResponse(
            id = log.id,
            resultEventId = log.resultEventId,
            rawEventId = log.rawEventId,
            provider = log.provider,
            endpointKey = log.endpointKey,
            apiId = log.apiId,
            canonicalHash = log.canonicalHash,
            rawJsonObjectKey = log.rawJsonObjectKey,
            scannerVersion = log.scannerVersion,
            checkedAt = log.checkedAt,
            issueCount = log.issueCount,
            hasIssue = log.hasIssue,
            result = objectMapper.readTree(log.resultJson),
            createdAt = log.createdAt,
            updatedAt = log.updatedAt,
        )

    private fun toRawJsonDownloadUrlResponse(
        downloadUrl: AdminDataQualityRawJsonDownloadUrlModel,
    ): AdminDataQualityRawJsonDownloadUrlResponse =
        AdminDataQualityRawJsonDownloadUrlResponse(
            downloadUrl = downloadUrl.downloadUrl,
            expiresAt = downloadUrl.expiresAt,
        )
}
