package com.footballay.core.infra.apisports.backbone.sync.league

import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsSeasonRepository
import com.footballay.core.infra.persistence.core.repository.LeagueSeasonCoreRepository
import org.springframework.stereotype.Component

@Component
class LeagueApiSportsSeasonSyncPlanSaver(
    private val leagueApiSportsSeasonRepository: LeagueApiSportsSeasonRepository,
    private val leagueSeasonCoreRepository: LeagueSeasonCoreRepository,
) {
    fun save(plan: LeagueApiSportsSeasonSyncPlan) {
        if (plan.coreSeasonsToSave.isNotEmpty()) {
            leagueSeasonCoreRepository.saveAll(plan.coreSeasonsToSave.values)
        }
        if (plan.providerSeasonsToSave.isNotEmpty()) {
            leagueApiSportsSeasonRepository.saveAll(plan.providerSeasonsToSave.values)
        }
    }
}
