package com.footballay.core.backbone.apisports.query

import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FixtureApiSportsQueryService(
    private val fixtureApiSportsRepository: FixtureApiSportsRepository,
) {
    @Transactional(readOnly = true)
    fun findApiIdByFixtureUid(fixtureUid: String): Long? =
        fixtureApiSportsRepository.findByCoreUid(fixtureUid)?.apiId
}
