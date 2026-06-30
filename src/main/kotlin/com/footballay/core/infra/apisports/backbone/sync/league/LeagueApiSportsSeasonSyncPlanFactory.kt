package com.footballay.core.infra.apisports.backbone.sync.league

import com.footballay.core.infra.apisports.shared.dto.LeagueApiSportsCoverageCreateDto
import com.footballay.core.infra.apisports.shared.dto.LeagueApiSportsSeasonCreateDto
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsCoverage
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsSeason
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.LeagueSeasonCore
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class LeagueApiSportsSeasonSyncPlanFactory {
    fun createPlan(command: LeagueApiSportsSeasonPlanCommand): LeagueApiSportsSeasonSyncPlan {
        val workingCoreSeasonsByIdentity = command.existingCoreSeasonsByIdentity.toMutableMap()
        val workingProviderSeasonsByIdentity = command.existingProviderSeasonsByIdentity.toMutableMap()
        val coreSeasonsToSave = linkedMapOf<CoreSeasonIdentity, LeagueSeasonCore>()
        val providerSeasonsToSave = linkedMapOf<ProviderSeasonIdentity, LeagueApiSportsSeason>()
        val resolvedPairs = mutableListOf<ResolvedSeasonPair>()

        command.inputs.forEach { input ->
            val leagueCore =
                requireNotNull(command.leagueCoreById[input.leagueCoreId]) {
                    "LeagueCore reference was not prepared. leagueCoreId=${input.leagueCoreId}"
                }
            val leagueApiSports =
                requireNotNull(command.leagueApiSportsById[input.leagueApiSportsId]) {
                    "LeagueApiSports reference was not prepared. leagueApiSportsId=${input.leagueApiSportsId}"
                }

            planCurrentFlagChanges(
                leagueCoreId = input.leagueCoreId,
                currentSeasonYear = input.leagueCreateDto.currentSeason,
                coreSeasonsByIdentity = workingCoreSeasonsByIdentity,
            ).forEach { changedCoreSeason ->
                coreSeasonsToSave[changedCoreSeason.identity] = changedCoreSeason.coreSeason
            }

            input.leagueCreateDto.seasons.forEach { seasonCreateDto ->
                val resolvedPair =
                    resolveSeasonPair(
                        seasonCreateDto = seasonCreateDto,
                        currentSeasonYear = input.leagueCreateDto.currentSeason,
                        leagueCore = leagueCore,
                        leagueCoreId = input.leagueCoreId,
                        leagueApiSports = leagueApiSports,
                        leagueApiSportsId = input.leagueApiSportsId,
                        coreSeasonsByIdentity = workingCoreSeasonsByIdentity,
                        providerSeasonsByIdentity = workingProviderSeasonsByIdentity,
                        coreSeasonsToSave = coreSeasonsToSave,
                        providerSeasonsToSave = providerSeasonsToSave,
                    )
                resolvedPairs.add(resolvedPair)
            }
        }

        return LeagueApiSportsSeasonSyncPlan(
            coreSeasonsToSave = coreSeasonsToSave,
            providerSeasonsToSave = providerSeasonsToSave,
            resolvedPairs = resolvedPairs,
        )
    }

    private fun planCurrentFlagChanges(
        leagueCoreId: Long,
        currentSeasonYear: Int?,
        coreSeasonsByIdentity: Map<CoreSeasonIdentity, LeagueSeasonCore>,
    ): List<ChangedCoreSeason> =
        coreSeasonsByIdentity
            .filterKeys { it.leagueCoreId == leagueCoreId }
            .mapNotNull { (identity, coreSeason) ->
                val shouldBeCurrent = identity.seasonYear == currentSeasonYear
                if (coreSeason.current == shouldBeCurrent) {
                    null
                } else {
                    coreSeason.current = shouldBeCurrent
                    ChangedCoreSeason(identity = identity, coreSeason = coreSeason)
                }
            }

    private fun resolveSeasonPair(
        seasonCreateDto: LeagueApiSportsSeasonCreateDto,
        currentSeasonYear: Int?,
        leagueCore: LeagueCore,
        leagueCoreId: Long,
        leagueApiSports: LeagueApiSports,
        leagueApiSportsId: Long,
        coreSeasonsByIdentity: MutableMap<CoreSeasonIdentity, LeagueSeasonCore>,
        providerSeasonsByIdentity: MutableMap<ProviderSeasonIdentity, LeagueApiSportsSeason>,
        coreSeasonsToSave: MutableMap<CoreSeasonIdentity, LeagueSeasonCore>,
        providerSeasonsToSave: MutableMap<ProviderSeasonIdentity, LeagueApiSportsSeason>,
    ): ResolvedSeasonPair {
        val providerSeasonValues =
            readProviderSeasonValues(
                leagueApiSportsId = leagueApiSportsId,
                currentSeasonYear = currentSeasonYear,
                seasonCreateDto = seasonCreateDto,
            )
        val coreIdentity =
            CoreSeasonIdentity(
                leagueCoreId = leagueCoreId,
                seasonYear = providerSeasonValues.seasonYear,
            )
        val providerIdentity =
            ProviderSeasonIdentity(
                leagueApiSportsId = leagueApiSportsId,
                seasonYear = providerSeasonValues.seasonYear,
            )

        val existingCoreSeason = coreSeasonsByIdentity[coreIdentity]
        val existingProviderSeason = providerSeasonsByIdentity[providerIdentity]
        val pairCase = classifySeasonPair(existingCoreSeason, existingProviderSeason)

        val coreSeason =
            existingCoreSeason
                ?: LeagueSeasonCore(
                    league = leagueCore,
                    seasonYear = coreIdentity.seasonYear,
                    autoGenerated = true,
                ).also { coreSeasonsByIdentity[coreIdentity] = it }
        if (updateCoreSeasonFields(coreSeason, providerSeasonValues)) {
            coreSeasonsToSave[coreIdentity] = coreSeason
        }

        val providerSeason =
            existingProviderSeason
                ?: LeagueApiSportsSeason(
                    seasonYear = providerIdentity.seasonYear,
                    leagueApiSports = leagueApiSports,
                ).also { providerSeasonsByIdentity[providerIdentity] = it }
        if (updateProviderSeasonFields(providerSeason, leagueApiSports, providerSeasonValues, coreSeason)) {
            providerSeasonsToSave[providerIdentity] = providerSeason
        }
        syncLeagueSeasonCollection(leagueApiSports, providerSeason)

        return ResolvedSeasonPair(
            coreIdentity = coreIdentity,
            providerIdentity = providerIdentity,
            case = pairCase,
            coreSeason = coreSeason,
            providerSeason = providerSeason,
        )
    }

    private fun classifySeasonPair(
        coreSeason: LeagueSeasonCore?,
        providerSeason: LeagueApiSportsSeason?,
    ): SeasonPairCase =
        when {
            coreSeason == null && providerSeason == null -> SeasonPairCase.CORE_MISSING_PROVIDER_MISSING
            coreSeason != null && providerSeason == null -> SeasonPairCase.CORE_EXISTS_PROVIDER_MISSING
            coreSeason == null && providerSeason != null -> SeasonPairCase.CORE_MISSING_PROVIDER_EXISTS
            else -> SeasonPairCase.CORE_EXISTS_PROVIDER_EXISTS
        }

    private fun updateCoreSeasonFields(
        coreSeason: LeagueSeasonCore,
        providerSeasonValues: ProviderSeasonValues,
    ): Boolean {
        var changed = coreSeason.id == null

        if (coreSeason.seasonStart != providerSeasonValues.seasonStart) {
            coreSeason.seasonStart = providerSeasonValues.seasonStart
            changed = true
        }
        if (coreSeason.seasonEnd != providerSeasonValues.seasonEnd) {
            coreSeason.seasonEnd = providerSeasonValues.seasonEnd
            changed = true
        }
        if (coreSeason.current != providerSeasonValues.current) {
            coreSeason.current = providerSeasonValues.current
            changed = true
        }

        return changed
    }

    private fun updateProviderSeasonFields(
        providerSeason: LeagueApiSportsSeason,
        leagueApiSports: LeagueApiSports,
        providerSeasonValues: ProviderSeasonValues,
        coreSeason: LeagueSeasonCore,
    ): Boolean {
        var changed = providerSeason.id == null

        if (providerSeason.seasonYear != providerSeasonValues.seasonYear) {
            providerSeason.seasonYear = providerSeasonValues.seasonYear
            changed = true
        }
        if (providerSeason.seasonStart != providerSeasonValues.seasonStart) {
            providerSeason.seasonStart = providerSeasonValues.seasonStart
            changed = true
        }
        if (providerSeason.seasonEnd != providerSeasonValues.seasonEnd) {
            providerSeason.seasonEnd = providerSeasonValues.seasonEnd
            changed = true
        }
        if (providerSeason.coverage != providerSeasonValues.coverage) {
            providerSeason.coverage = providerSeasonValues.coverage
            changed = true
        }
        if (providerSeason.leagueApiSports?.id != leagueApiSports.id) {
            providerSeason.leagueApiSports = leagueApiSports
            changed = true
        }
        if (!sameCoreSeason(providerSeason.leagueSeasonCore, coreSeason)) {
            providerSeason.leagueSeasonCore = coreSeason
            changed = true
        }

        return changed
    }

    private fun syncLeagueSeasonCollection(
        leagueApiSports: LeagueApiSports,
        providerSeason: LeagueApiSportsSeason,
    ) {
        if (leagueApiSports.seasons.none { sameProviderSeason(it, providerSeason) }) {
            leagueApiSports.seasons = leagueApiSports.seasons + providerSeason
        }
    }

    private fun sameProviderSeason(
        left: LeagueApiSportsSeason,
        right: LeagueApiSportsSeason,
    ): Boolean {
        val leftId = left.id
        val rightId = right.id
        return if (leftId != null && rightId != null) {
            leftId == rightId
        } else {
            left === right ||
                (
                    left.leagueApiSports?.id == right.leagueApiSports?.id &&
                        left.seasonYear == right.seasonYear
                )
        }
    }

    private fun readProviderSeasonValues(
        leagueApiSportsId: Long,
        currentSeasonYear: Int?,
        seasonCreateDto: LeagueApiSportsSeasonCreateDto,
    ): ProviderSeasonValues {
        val seasonYear =
            requireNotNull(seasonCreateDto.seasonYear) {
                "LeagueApiSports seasonYear is required. leagueApiSportsId=$leagueApiSportsId"
            }
        return ProviderSeasonValues(
            seasonYear = seasonYear,
            seasonStart = seasonCreateDto.seasonStart?.let { LocalDate.parse(it) },
            seasonEnd = seasonCreateDto.seasonEnd?.let { LocalDate.parse(it) },
            coverage = seasonCreateDto.coverage?.let { createLeagueApiSportsCoverage(it) },
            current = seasonYear == currentSeasonYear,
        )
    }

    private fun sameCoreSeason(
        left: LeagueSeasonCore?,
        right: LeagueSeasonCore,
    ): Boolean {
        val leftId = left?.id
        val rightId = right.id
        return if (leftId != null && rightId != null) {
            leftId == rightId
        } else {
            left === right
        }
    }

    private fun createLeagueApiSportsCoverage(coverageCreateDto: LeagueApiSportsCoverageCreateDto) =
        LeagueApiSportsCoverage(
            fixturesEvents = coverageCreateDto.fixturesEvents,
            fixturesLineups = coverageCreateDto.fixturesLineups,
            fixturesStatistics = coverageCreateDto.fixturesStatistics,
            fixturesPlayers = coverageCreateDto.fixturesPlayers,
            standings = coverageCreateDto.standings,
            players = coverageCreateDto.players,
            topScorers = coverageCreateDto.topScorers,
            topAssists = coverageCreateDto.topAssists,
            topCards = coverageCreateDto.topCards,
            injuries = coverageCreateDto.injuries,
            predictions = coverageCreateDto.predictions,
            odds = coverageCreateDto.odds,
        )

    private data class ChangedCoreSeason(
        val identity: CoreSeasonIdentity,
        val coreSeason: LeagueSeasonCore,
    )

    private data class ProviderSeasonValues(
        val seasonYear: Int,
        val seasonStart: LocalDate?,
        val seasonEnd: LocalDate?,
        val coverage: LeagueApiSportsCoverage?,
        val current: Boolean,
    )
}
