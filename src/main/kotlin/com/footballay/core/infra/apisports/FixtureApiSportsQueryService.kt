package com.footballay.core.infra.apisports

import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class FixtureApiSportsQueryService(
    private val fixtureCoreRepository: FixtureCoreRepository,
    private val fixtureApiSportsRepository: FixtureApiSportsRepository
) {

    @Transactional
    fun getApiIdByFixtureUid(fixtureUid: String): FixtureApiSports? {
        return fixtureApiSportsRepository.findByCoreUid(fixtureUid)
    }

}