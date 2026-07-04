package com.footballay.core.web.admin.dataquality.dto

import com.fasterxml.jackson.databind.JsonNode
import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import java.time.Instant

data class AdminDataQualityLogPageResponse(
    val content: List<AdminDataQualityLogSummaryResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

data class AdminDataQualityLogSummaryResponse(
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
    val hasIssue: Boolean,
    val createdAt: Instant,
)

data class AdminDataQualityLogDetailResponse(
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
    val hasIssue: Boolean,
    val result: JsonNode,
    val createdAt: Instant,
    val updatedAt: Instant,
)
