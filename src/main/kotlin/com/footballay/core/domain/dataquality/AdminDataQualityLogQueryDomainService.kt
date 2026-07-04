package com.footballay.core.domain.dataquality

import com.footballay.core.infra.persistence.dataquality.entity.DataQualityResultLog
import com.footballay.core.infra.persistence.dataquality.repository.DataQualityResultLogRepository
import com.footballay.core.infra.dataquality.raw.RawResponseStorage
import com.footballay.core.infra.dataquality.raw.model.RawResponseDownloadUrlCommand
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

/**
 * Data Quality 로그 조회에 필요한 재사용 가능한 도메인 조회 로직입니다.
 */
@Service
class AdminDataQualityLogQueryDomainService(
    private val repository: DataQualityResultLogRepository,
    private val rawResponseStorage: RawResponseStorage,
) {
    fun findLogs(
        filter: AdminDataQualityLogQueryFilter,
        pageable: Pageable,
    ): Page<AdminDataQualityLogSummaryModel> {
        validateFindLogsRequest(filter, pageable)

        return repository
            .findLogs(
                provider = filter.provider,
                endpointKey = filter.endpointKey,
                apiId = filter.apiId,
                checkedAtFrom = filter.checkedAtFrom,
                checkedAtTo = filter.checkedAtTo,
                hasIssue = filter.hasIssue,
                pageable = pageable,
            ).map(::toSummaryModel)
    }

    fun findLog(id: Long): AdminDataQualityLogDetailModel? {
        require(id > 0) { "id must be positive" }

        return repository
            .findById(id)
            .map(::toDetailModel)
            .orElse(null)
    }

    fun createRawJsonDownloadUrl(id: Long): AdminDataQualityRawJsonDownloadUrlModel? {
        require(id > 0) { "id must be positive" }

        val rawJsonObjectKey =
            repository
                .findById(id)
                .map { it.rawJsonObjectKey }
                .orElse(null)
                ?: return null

        val downloadUrl =
            rawResponseStorage.createDownloadUrl(
                RawResponseDownloadUrlCommand(rawJsonObjectKey = rawJsonObjectKey),
            )

        return AdminDataQualityRawJsonDownloadUrlModel(
            downloadUrl = downloadUrl.downloadUrl,
            expiresAt = downloadUrl.expiresAt,
        )
    }

    private fun validateFindLogsRequest(
        filter: AdminDataQualityLogQueryFilter,
        pageable: Pageable,
    ) {
        require(filter.endpointKey == null || filter.endpointKey.isNotBlank()) {
            "endpointKey must not be blank"
        }
        require(filter.apiId == null || filter.apiId.isNotBlank()) {
            "apiId must not be blank"
        }
        if (filter.checkedAtFrom != null && filter.checkedAtTo != null) {
            require(!filter.checkedAtFrom.isAfter(filter.checkedAtTo)) {
                "checkedAtFrom must be before or equal to checkedAtTo"
            }
        }
        require(pageable.isPaged) {
            "pageable must be paged"
        }
        require(pageable.pageNumber >= 0) {
            "page must be greater than or equal to 0"
        }
        require(pageable.pageSize in 1..200) {
            "size must be between 1 and 200"
        }
    }

    private fun toSummaryModel(log: DataQualityResultLog): AdminDataQualityLogSummaryModel =
        AdminDataQualityLogSummaryModel(
            id = requireNotNull(log.id),
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
            createdAt = log.createdAt,
        )

    private fun toDetailModel(log: DataQualityResultLog): AdminDataQualityLogDetailModel =
        AdminDataQualityLogDetailModel(
            id = requireNotNull(log.id),
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
            resultJson = log.resultJson,
            createdAt = log.createdAt,
            updatedAt = log.updatedAt,
        )
}
