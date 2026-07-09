package com.footballay.core.infra.dataquality.result.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

// Data Quality 검사 결과를 저장하는 MongoDB quality_results document schema를 정의한다.
object DataQualityMongoCollections {
    const val QUALITY_RESULTS = "quality_results"
}

@Document(collection = DataQualityMongoCollections.QUALITY_RESULTS)
data class QualityResultDocument(
    @Id
    @param:JsonProperty("_id")
    @get:JsonProperty("_id")
    val id: String,
    val rawEventId: String,
    val provider: FootballDataProvider,
    val endpointKey: String,
    val parameters: List<QualityResultParameterDocument>,
    val canonicalHash: String,
    val rawJsonObjectKey: String,
    val checkedAt: Instant,
    val scannerVersion: String,
    val hasIssue: Boolean,
    val issueCount: Int,
    val maxSeverity: DataQualityMaxSeverity,
    val checkStatus: DataQualityCheckStatus,
    val issues: List<QualityIssueDocument>,
    val archive: QualityResultArchiveDocument,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class QualityResultParameterDocument(
    val name: String,
    val value: String,
)

data class QualityIssueDocument(
    val issueInstanceId: String,
    val suggestedTypeCode: String,
    val confirmedTypeCode: String?,
    val checkStatus: DataQualityIssueCheckStatus,
    val severity: DataQualityIssueSeverity,
    val title: String,
    val responseLocation: QualityIssueResponseLocationDocument,
    val evidence: Map<String, Any?>,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class QualityIssueResponseLocationDocument(
    val section: String,
    val path: String,
)

data class QualityResultArchiveDocument(
    val status: DataQualityArchiveStatus = DataQualityArchiveStatus.NONE,
    val objectKey: String? = null,
    val archivedAt: Instant? = null,
    val expiredAt: Instant? = null,
)

enum class DataQualityMaxSeverity {
    NONE,
    INFO,
    WARN,
    ERROR,
    CRITICAL,
}

enum class DataQualityIssueSeverity {
    INFO,
    WARN,
    ERROR,
    CRITICAL,
}

enum class DataQualityCheckStatus {
    NO_ISSUE,
    NEED_CHECK,
    PARTIALLY_CHECKED,
    ALL_CHECKED,
    CHECK_HOLD,
}

enum class DataQualityIssueCheckStatus {
    NEED_CHECK,
    ALL_CHECKED,
    CHECK_HOLD,
}

enum class DataQualityArchiveStatus {
    NONE,
    ARCHIVED,
    EXPIRED,
    FAILED,
}
