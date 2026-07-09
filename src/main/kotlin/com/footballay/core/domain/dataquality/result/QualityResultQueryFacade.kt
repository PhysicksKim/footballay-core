package com.footballay.core.domain.dataquality.result

import com.footballay.core.infra.dataquality.result.model.QualityResultDocument
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class QualityResultQueryFacade(
    private val qualityResultRepositoryPort: QualityResultRepositoryPort,
) {
    fun findPage(
        condition: QualityResultSearchCondition,
        pageable: Pageable,
    ): Page<QualityResultDocument> = qualityResultRepositoryPort.findPage(condition, pageable)

    fun findById(resultId: String): QualityResultDocument =
        qualityResultRepositoryPort.findById(resultId)
            ?: throw NoSuchElementException("Quality result not found: $resultId")
}
