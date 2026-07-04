package com.footballay.core.domain.dataquality

import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import java.time.Instant

/**
 * Data Quality 로그 목록 조회 조건입니다.
 */
data class AdminDataQualityLogQueryFilter(
    val provider: FootballDataProvider?,
    val endpointKey: String?,
    val apiId: String?,
    val checkedAtFrom: Instant?,
    val checkedAtTo: Instant?,
    val hasIssue: Boolean?,
)

/**
 * Data Quality 로그 목록 응답에 사용하는 요약 도메인 모델입니다.
 */
data class AdminDataQualityLogSummaryModel(
    val id: Long,
    val resultEventId: String,
    val rawEventId: String,
    val provider: FootballDataProvider,
    val endpointKey: String,
    val apiId: String,
    val canonicalHash: String,
    val rawJsonObjectKey: String,
    val scannerVersion: String,
    val checkedAt: Instant,
    val issueCount: Int,
    val createdAt: Instant,
) {
    val hasIssue: Boolean = issueCount > 0
}

/**
 * Data Quality 로그 상세 응답에 사용하는 도메인 모델입니다.
 */
data class AdminDataQualityLogDetailModel(
    val id: Long,
    val resultEventId: String,
    val rawEventId: String,
    val provider: FootballDataProvider,
    val endpointKey: String,
    val apiId: String,
    val canonicalHash: String,
    val rawJsonObjectKey: String,
    val scannerVersion: String,
    val checkedAt: Instant,
    val issueCount: Int,
    val resultJson: String,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val hasIssue: Boolean = issueCount > 0
}
