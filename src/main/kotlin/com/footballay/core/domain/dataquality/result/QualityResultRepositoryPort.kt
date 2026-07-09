package com.footballay.core.domain.dataquality.result

import com.footballay.core.infra.dataquality.result.model.QualityResultDocument
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface QualityResultRepositoryPort {
    fun findPage(
        condition: QualityResultSearchCondition,
        pageable: Pageable,
    ): Page<QualityResultDocument>

    fun findById(resultId: String): QualityResultDocument?
}
