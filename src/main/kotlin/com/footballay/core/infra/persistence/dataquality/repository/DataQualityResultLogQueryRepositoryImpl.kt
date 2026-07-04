package com.footballay.core.infra.persistence.dataquality.repository

import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import com.footballay.core.infra.persistence.dataquality.entity.DataQualityResultLog
import com.footballay.core.infra.persistence.dataquality.entity.QDataQualityResultLog
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.Instant

class DataQualityResultLogQueryRepositoryImpl(
    entityManager: EntityManager,
) : DataQualityResultLogQueryRepository {
    private val dataQualityResultLog = QDataQualityResultLog("dataQualityResultLog")

    private val queryFactory = JPAQueryFactory(entityManager)

    override fun findLogs(
        provider: FootballDataProvider?,
        endpointKey: String?,
        apiId: String?,
        checkedAtFrom: Instant?,
        checkedAtTo: Instant?,
        hasIssue: Boolean?,
        pageable: Pageable,
    ): Page<DataQualityResultLog> {
        val conditions: BooleanBuilder =
            conditions(
                provider = provider,
                endpointKey = endpointKey,
                apiId = apiId,
                checkedAtFrom = checkedAtFrom,
                checkedAtTo = checkedAtTo,
                hasIssue = hasIssue,
            )

        val content: List<DataQualityResultLog> =
            queryFactory
                .selectFrom(dataQualityResultLog)
                .where(conditions)
                .orderBy(*orderSpecifiers(pageable))
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        val total: Long =
            queryFactory
                .select(dataQualityResultLog.id.count())
                .from(dataQualityResultLog)
                .where(conditions)
                .fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }

    private fun conditions(
        provider: FootballDataProvider?,
        endpointKey: String?,
        apiId: String?,
        checkedAtFrom: Instant?,
        checkedAtTo: Instant?,
        hasIssue: Boolean?,
    ): BooleanBuilder =
        BooleanBuilder().apply {
            provider?.let { and(dataQualityResultLog.provider.eq(it)) }

            endpointKey?.takeIf { it.isNotBlank() }?.let { and(dataQualityResultLog.endpointKey.eq(it)) }
            apiId?.takeIf { it.isNotBlank() }?.let { and(dataQualityResultLog.apiId.eq(it)) }

            checkedAtFrom?.let { and(dataQualityResultLog.checkedAt.goe(it)) }
            checkedAtTo?.let { and(dataQualityResultLog.checkedAt.loe(it)) }

            hasIssue?.let {
                if (it) {
                    and(dataQualityResultLog.issueCount.gt(0))
                } else {
                    and(dataQualityResultLog.issueCount.eq(0))
                }
            }
        }

    private fun orderSpecifiers(pageable: Pageable): Array<OrderSpecifier<*>> {
        val orders =
            pageable.sort
                .map { order ->
                    when (order.property) {
                        "id" -> if (order.isAscending) dataQualityResultLog.id.asc() else dataQualityResultLog.id.desc()
                        "provider" -> if (order.isAscending) dataQualityResultLog.provider.asc() else dataQualityResultLog.provider.desc()
                        "endpointKey" -> if (order.isAscending) dataQualityResultLog.endpointKey.asc() else dataQualityResultLog.endpointKey.desc()
                        "apiId" -> if (order.isAscending) dataQualityResultLog.apiId.asc() else dataQualityResultLog.apiId.desc()
                        "checkedAt" -> if (order.isAscending) dataQualityResultLog.checkedAt.asc() else dataQualityResultLog.checkedAt.desc()
                        "issueCount" -> if (order.isAscending) dataQualityResultLog.issueCount.asc() else dataQualityResultLog.issueCount.desc()
                        "createdAt" -> if (order.isAscending) dataQualityResultLog.createdAt.asc() else dataQualityResultLog.createdAt.desc()
                        else -> throw IllegalArgumentException("Unsupported data quality log sort property: ${order.property}")
                    }
                }.toList()

        return (orders.ifEmpty { listOf(dataQualityResultLog.checkedAt.desc(), dataQualityResultLog.id.desc()) }).toTypedArray()
    }
}
