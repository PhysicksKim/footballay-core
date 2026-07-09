package com.footballay.core.infra.dataquality.result

import com.footballay.core.domain.dataquality.result.QualityResultRepositoryPort
import com.footballay.core.domain.dataquality.result.QualityResultSearchCondition
import com.footballay.core.infra.dataquality.result.model.QualityResultDocument
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class MongoQualityResultRepositoryAdapter(
    private val qualityResultMongoRepository: QualityResultMongoRepository,
) : QualityResultRepositoryPort {
    override fun findPage(
        condition: QualityResultSearchCondition,
        pageable: Pageable,
    ): Page<QualityResultDocument> = qualityResultMongoRepository.findPage(condition, pageable)

    override fun findById(resultId: String): QualityResultDocument? = qualityResultMongoRepository.findById(resultId).orElse(null)
}
