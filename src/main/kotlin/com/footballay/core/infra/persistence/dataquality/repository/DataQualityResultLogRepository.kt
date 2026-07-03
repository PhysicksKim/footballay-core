package com.footballay.core.infra.persistence.dataquality.repository

import com.footballay.core.infra.persistence.dataquality.entity.DataQualityResultLog
import org.springframework.data.jpa.repository.JpaRepository

interface DataQualityResultLogRepository : JpaRepository<DataQualityResultLog, Long> {
    fun existsByResultEventId(resultEventId: String): Boolean
}

