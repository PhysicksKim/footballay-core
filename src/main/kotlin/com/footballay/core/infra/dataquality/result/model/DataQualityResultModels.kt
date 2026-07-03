package com.footballay.core.infra.dataquality.result.model

import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import java.time.Instant

data class DataQualityIssueItem(
    val code: String,
    val severity: String,
    val classification: String,
    val path: String,
    val message: String,
    val evidence: Map<String, Any?> = emptyMap(),
)

data class DataQualityResultSummary(
    val issueCount: Int,
)

data class DataQualityResultMessage(
    val schemaVersion: Int = 1,
    val eventId: String,
    val sourceEventId: String,
    val provider: FootballDataProvider,
    val endpointKey: String,
    val apiId: String,
    val canonicalHash: String,
    val rawJsonObjectKey: String,
    val checkedAt: Instant,
    val scannerVersion: String,
    val summary: DataQualityResultSummary,
    val items: List<DataQualityIssueItem> = emptyList(),
)

