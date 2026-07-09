package com.footballay.core.infra.dataquality.result

import com.footballay.core.domain.dataquality.result.QualityResultSearchCondition
import com.footballay.core.infra.dataquality.result.model.QualityResultDocument
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface QualityResultMongoRepositoryCustom {
    fun findPage(
        condition: QualityResultSearchCondition,
        pageable: Pageable,
    ): Page<QualityResultDocument>
}
