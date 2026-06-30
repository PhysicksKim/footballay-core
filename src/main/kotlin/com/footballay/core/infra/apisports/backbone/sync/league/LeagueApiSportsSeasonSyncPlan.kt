package com.footballay.core.infra.apisports.backbone.sync.league

import com.footballay.core.infra.apisports.shared.dto.LeagueApiSportsCreateDto
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsSeason
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.LeagueSeasonCore

data class LeagueSeasonSyncInput(
    val leagueCreateDto: LeagueApiSportsCreateDto,
    val leagueApiSportsId: Long,
    val leagueCoreId: Long,
)

data class CoreSeasonIdentity(
    val leagueCoreId: Long,
    val seasonYear: Int,
)

data class ProviderSeasonIdentity(
    val leagueApiSportsId: Long,
    val seasonYear: Int,
)

enum class SeasonPairCase {
    CORE_MISSING_PROVIDER_MISSING,
    CORE_EXISTS_PROVIDER_MISSING,
    CORE_MISSING_PROVIDER_EXISTS,
    CORE_EXISTS_PROVIDER_EXISTS,
}

data class ResolvedSeasonPair(
    val coreIdentity: CoreSeasonIdentity,
    val providerIdentity: ProviderSeasonIdentity,
    val case: SeasonPairCase,
    val coreSeason: LeagueSeasonCore,
    val providerSeason: LeagueApiSportsSeason,
)

data class LeagueApiSportsSeasonSyncPlan(
    val coreSeasonsToSave: Map<CoreSeasonIdentity, LeagueSeasonCore>,
    val providerSeasonsToSave: Map<ProviderSeasonIdentity, LeagueApiSportsSeason>,
    val resolvedPairs: List<ResolvedSeasonPair>,
)

data class LeagueApiSportsSeasonPlanCommand(
    val inputs: List<LeagueSeasonSyncInput>,
    val leagueApiSportsById: Map<Long, LeagueApiSports>,
    val leagueCoreById: Map<Long, LeagueCore>,
    val existingCoreSeasonsByIdentity: Map<CoreSeasonIdentity, LeagueSeasonCore>,
    val existingProviderSeasonsByIdentity: Map<ProviderSeasonIdentity, LeagueApiSportsSeason>,
)
