package com.footballay.core.domain.dataquality.result

import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import com.footballay.core.infra.dataquality.result.model.DataQualityArchiveStatus
import com.footballay.core.infra.dataquality.result.model.DataQualityCheckStatus
import com.footballay.core.infra.dataquality.result.model.DataQualityMaxSeverity
import java.time.Instant

// Admin Data Quality result 조회 조건을 domain 경계에서 표현한다.
data class QualityResultSearchCondition(
    val provider: FootballDataProvider? = null,
    val endpointKey: String? = null,
    val checkedAtFrom: Instant? = null,
    val checkedAtTo: Instant? = null,
    val hasIssue: Boolean? = null,
    val maxSeverity: DataQualityMaxSeverity? = null,
    val checkStatus: DataQualityCheckStatus? = null,
    val suggestedTypeCode: String? = null,
    val confirmedTypeCode: String? = null,
    val archiveStatus: DataQualityArchiveStatus? = null,
    val parameterName: String? = null,
    val parameterValue: String? = null,
) {
    init {
        require(
            checkedAtFrom == null ||
                checkedAtTo == null ||
                !checkedAtFrom.isAfter(checkedAtTo),
        ) {
            "checkedAtFrom must be before or equal to checkedAtTo"
        }
        require(!parameterName.isNullOrBlank() || parameterValue.isNullOrBlank()) {
            "parameterName is required when parameterValue is provided"
        }
    }
}
