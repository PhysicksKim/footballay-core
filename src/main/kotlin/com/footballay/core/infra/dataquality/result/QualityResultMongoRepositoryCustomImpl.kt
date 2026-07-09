package com.footballay.core.infra.dataquality.result

import com.footballay.core.domain.dataquality.result.QualityResultSearchCondition
import com.footballay.core.infra.dataquality.result.model.DataQualityMongoCollections
import com.footballay.core.infra.dataquality.result.model.QualityResultDocument
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

class QualityResultMongoRepositoryCustomImpl(
    private val mongoTemplate: MongoTemplate,
) : QualityResultMongoRepositoryCustom {
    override fun findPage(
        condition: QualityResultSearchCondition,
        pageable: Pageable,
    ): Page<QualityResultDocument> {
        val baseQuery = buildQuery(condition)
        val total = mongoTemplate.count(baseQuery, QualityResultDocument::class.java, DataQualityMongoCollections.QUALITY_RESULTS)
        val pageQuery =
            buildQuery(condition)
                .with(defaultSort(pageable))
                .with(pageable)
        val content = mongoTemplate.find(pageQuery, QualityResultDocument::class.java, DataQualityMongoCollections.QUALITY_RESULTS)
        return PageImpl(content, pageable, total)
    }

    private fun buildQuery(condition: QualityResultSearchCondition): Query {
        val criteria =
            basicCriteria(condition) +
                issueCriteria(condition) +
                archiveCriteria(condition) +
                listOfNotNull(
                    checkedAtCriteria(condition),
                    parameterCriteria(condition),
                )

        return when (criteria.size) {
            0 -> Query()
            1 -> Query(criteria.single())
            else -> Query(Criteria().andOperator(criteria))
        }
    }

    private fun basicCriteria(condition: QualityResultSearchCondition): List<Criteria> =
        listOfNotNull(
            condition.provider?.let { Criteria.where("provider").`is`(it) },
            condition.endpointKey?.takeIf { it.isNotBlank() }?.let { Criteria.where("endpointKey").`is`(it) },
            condition.hasIssue?.let { Criteria.where("hasIssue").`is`(it) },
            condition.maxSeverity?.let { Criteria.where("maxSeverity").`is`(it) },
            condition.checkStatus?.let { Criteria.where("checkStatus").`is`(it) },
        )

    private fun issueCriteria(condition: QualityResultSearchCondition): List<Criteria> =
        listOfNotNull(
            condition.suggestedTypeCode
                ?.takeIf { it.isNotBlank() }
                ?.let { Criteria.where("issues.suggestedTypeCode").`is`(it) },
            condition.confirmedTypeCode
                ?.takeIf { it.isNotBlank() }
                ?.let { Criteria.where("issues.confirmedTypeCode").`is`(it) },
        )

    private fun archiveCriteria(condition: QualityResultSearchCondition): List<Criteria> =
        listOfNotNull(
            condition.archiveStatus?.let { Criteria.where("archive.status").`is`(it) },
        )

    private fun checkedAtCriteria(condition: QualityResultSearchCondition): Criteria? {
        val from = condition.checkedAtFrom
        val to = condition.checkedAtTo
        if (from == null && to == null) {
            return null
        }

        val criteria = Criteria.where("checkedAt")
        from?.let { criteria.gte(it) }
        to?.let { criteria.lte(it) }
        return criteria
    }

    private fun parameterCriteria(condition: QualityResultSearchCondition): Criteria? {
        val name = condition.parameterName?.takeIf { it.isNotBlank() }
        val value = condition.parameterValue?.takeIf { it.isNotBlank() }
        if (name == null && value == null) {
            return null
        }

        val elemMatchCriteria = Criteria()
        name?.let { elemMatchCriteria.and("name").`is`(it) }
        value?.let { elemMatchCriteria.and("value").`is`(it) }
        return Criteria.where("parameters").elemMatch(elemMatchCriteria)
    }

    private fun defaultSort(pageable: Pageable): Sort =
        if (pageable.sort.isSorted) {
            pageable.sort
        } else {
            Sort.by(Sort.Direction.DESC, "checkedAt")
        }
}
