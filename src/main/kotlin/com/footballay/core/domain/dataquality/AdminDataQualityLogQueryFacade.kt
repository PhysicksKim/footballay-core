package com.footballay.core.domain.dataquality

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Data Quality 로그 조회를 웹, 배치, 스케줄러에서 재사용하기 위한 진입점입니다.
 */
@Component
class AdminDataQualityLogQueryFacade(
    private val domainService: AdminDataQualityLogQueryDomainService,
) {
    @Transactional(readOnly = true)
    fun findLogs(
        filter: AdminDataQualityLogQueryFilter,
        pageable: Pageable,
    ): Page<AdminDataQualityLogSummaryModel> =
        domainService.findLogs(
            filter = filter,
            pageable = pageable,
        )

    @Transactional(readOnly = true)
    fun getLog(id: Long): AdminDataQualityLogDetailModel =
        domainService.findLog(id)
            ?: throw NoSuchElementException("Data quality log not found: $id")

    @Transactional(readOnly = true)
    fun createRawJsonDownloadUrl(id: Long): AdminDataQualityRawJsonDownloadUrlModel =
        domainService.createRawJsonDownloadUrl(id)
            ?: throw NoSuchElementException("Data quality log not found: $id")
}
