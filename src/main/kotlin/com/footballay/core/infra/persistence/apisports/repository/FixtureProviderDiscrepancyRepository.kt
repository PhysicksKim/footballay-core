package com.footballay.core.infra.persistence.apisports.repository

import com.footballay.core.infra.persistence.apisports.entity.DataProvider
import com.footballay.core.infra.persistence.apisports.entity.DiscrepancyState
import com.footballay.core.infra.persistence.apisports.entity.FixtureProviderDiscrepancy
import org.springframework.data.jpa.repository.JpaRepository

interface FixtureProviderDiscrepancyRepository : JpaRepository<FixtureProviderDiscrepancy, Long> {
    fun findByProviderAndFixtureApiId(provider: DataProvider, fixtureApiId: Long): FixtureProviderDiscrepancy?
    fun findAllByProviderAndLeagueApiIdAndSeasonYearAndState(
        provider: DataProvider,
        leagueApiId: Long,
        seasonYear: String,
        state: DiscrepancyState,
    ): List<FixtureProviderDiscrepancy>
}


