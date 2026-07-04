package com.footballay.core.infra.persistence.dataquality.repository

import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import com.footballay.core.infra.persistence.dataquality.entity.DataQualityResultLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.Instant

interface DataQualityResultLogQueryRepository {
    fun findLogs(
        provider: FootballDataProvider?,
        endpointKey: String?,
        apiId: String?,
        checkedAtFrom: Instant?,
        checkedAtTo: Instant?,
        hasIssue: Boolean?,
        pageable: Pageable,
    ): Page<DataQualityResultLog>
}
