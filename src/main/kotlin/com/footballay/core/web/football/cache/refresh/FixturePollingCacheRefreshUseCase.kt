package com.footballay.core.web.football.cache.refresh

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.common.result.map
import com.footballay.core.infra.query.MatchDataQueryService
import com.footballay.core.logger
import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.football.cache.FixturePollingEndpoint
import com.footballay.core.web.football.cache.FixtureWebCacheIdentity
import com.footballay.core.web.football.cache.FixtureWebCacheManager
import com.footballay.core.web.football.cache.hash.FixtureResponseCacheDocument
import com.footballay.core.web.football.cache.hash.FixtureResponseCacheDocumentFactory
import com.footballay.core.web.football.localization.FootballResponseLocalizationService
import com.footballay.core.web.football.mapper.MatchDataMapper
import org.springframework.stereotype.Service

@Service
class FixturePollingCacheRefreshUseCase(
    private val matchDataQueryService: MatchDataQueryService,
    private val matchDataMapper: MatchDataMapper,
    private val localizationService: FootballResponseLocalizationService,
    private val cacheDocumentFactory: FixtureResponseCacheDocumentFactory,
    private val cacheManager: FixtureWebCacheManager,
) {
    private val log = logger()

    fun refreshAll(trigger: FixtureMatchCacheRefreshTrigger) {
        val fixtureUid = trigger.fixtureUid

        val liveStatus = query(
            endpoint = FixturePollingEndpoint.STATUS,
            query = {
                matchDataQueryService
                    .getFixtureLiveStatus(fixtureUid)
                    .map { domain -> matchDataMapper.toFixtureLiveStatusResponse(domain) }
            },
        )
        val lineup = query(
            endpoint = FixturePollingEndpoint.LINEUP,
            query = {
                matchDataQueryService
                    .getFixtureLineup(fixtureUid)
            },
        )
        val events = query(
            endpoint = FixturePollingEndpoint.EVENTS,
            query = {
                matchDataQueryService
                    .getFixtureEvents(fixtureUid)
            },
        )
        val statistics = query(
            endpoint = FixturePollingEndpoint.STATISTICS,
            query = {
                matchDataQueryService
                    .getFixtureStatistics(fixtureUid)
            },
        )

        // Update liveStatus cache ; LiveStatus response doesn't need localization
        liveStatus?.let { response ->
            save(
                identity = FixtureWebCacheIdentity(fixtureUid, FixturePollingEndpoint.STATUS, null),
                response = response,
                createDocument = cacheDocumentFactory::create,
            )
        }

        val localizedModels =
            runCatching {
                localizationService.preparePollingModels(
                    lineup = lineup,
                    events = events,
                    statistics = statistics,
                    locales = SupportedLocale.entries,
                )
            }.onFailure { ex ->
                log.warn("Failed to prepare localized fixture cache snapshots. fixtureUid={}", fixtureUid, ex)
            }.getOrNull()
                ?: return

        for (locale in SupportedLocale.entries) {
            val models = localizedModels[locale] ?: continue
            val lineupIdentity = FixtureWebCacheIdentity(fixtureUid, FixturePollingEndpoint.LINEUP, locale)
            val eventsIdentity = FixtureWebCacheIdentity(fixtureUid, FixturePollingEndpoint.EVENTS, locale)
            val statisticsIdentity = FixtureWebCacheIdentity(fixtureUid, FixturePollingEndpoint.STATISTICS, locale)

            models.lineup?.let { model ->
                save(lineupIdentity, model) {
                    cacheDocumentFactory.create(matchDataMapper.toFixtureLineupResponse(it))
                }
            }
            models.events?.let { model ->
                save(eventsIdentity, model) {
                    cacheDocumentFactory.create(matchDataMapper.toFixtureEventsResponse(it))
                }
            }
            models.statistics?.let { model ->
                save(statisticsIdentity, model) {
                    cacheDocumentFactory.create(matchDataMapper.toFixtureStatisticsResponse(it))
                }
            }
        }
    }

    private fun <T : Any> query(
        endpoint: FixturePollingEndpoint,
        query: () -> DomainResult<T, DomainFail>,
    ): T? =
        when (val result = query()) {
            is DomainResult.Success -> result.value
            is DomainResult.Fail -> {
                log.warn(
                    "Failed to query fixture cache snapshot. endpoint={}, error={}",
                    endpoint,
                    result.error,
                )
                null
            }
        }

    private fun <T : Any> save(
        identity: FixtureWebCacheIdentity,
        response: T,
        createDocument: (T) -> FixtureResponseCacheDocument,
    ) {
        runCatching {
            val document = createDocument(response)
            cacheManager.save(identity, document)
            log.info(
                "Refreshed fixture cache snapshot. fixtureUid={}, endpoint={}, locale={}",
                identity.fixtureUid,
                identity.endpoint,
                identity.locale,
            )
        }.onFailure { ex ->
            log.warn(
                "Failed to refresh fixture cache snapshot. fixtureUid={}, endpoint={}, locale={}",
                identity.fixtureUid,
                identity.endpoint,
                identity.locale,
                ex,
            )
        }
    }
}
