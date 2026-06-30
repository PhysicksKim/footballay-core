package com.footballay.core.web.football.cache.refresh

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.common.result.map
import com.footballay.core.infra.query.MatchDataQueryService
import com.footballay.core.logger
import com.footballay.core.web.football.cache.FixturePollingEndpoint
import com.footballay.core.web.football.cache.FixtureWebCacheManager
import com.footballay.core.web.football.cache.hash.FixtureResponseCacheDocument
import com.footballay.core.web.football.cache.hash.FixtureResponseCacheDocumentFactory
import com.footballay.core.web.football.dto.FixtureEventsResponse
import com.footballay.core.web.football.dto.FixtureLineupResponse
import com.footballay.core.web.football.dto.FixtureLiveStatusResponse
import com.footballay.core.web.football.dto.FixtureStatisticsResponse
import com.footballay.core.web.football.mapper.MatchDataMapper
import org.springframework.stereotype.Service

@Service
class FixtureSnapshotRefreshService(
    private val matchDataQueryService: MatchDataQueryService,
    private val matchDataMapper: MatchDataMapper,
    private val cacheDocumentFactory: FixtureResponseCacheDocumentFactory,
    private val cacheManager: FixtureWebCacheManager,
) {
    private val log = logger()

    fun refreshAll(trigger: FixtureMatchCacheRefreshTrigger) {
        val fixtureUid = trigger.fixtureUid

        refresh(
            fixtureUid = fixtureUid,
            endpoint = FixturePollingEndpoint.STATUS,
            query = {
                matchDataQueryService
                    .getFixtureLiveStatus(fixtureUid)
                    .map { domain -> matchDataMapper.toFixtureLiveStatusResponse(domain) }
            },
            createDocument = { response: FixtureLiveStatusResponse -> cacheDocumentFactory.create(response) },
        )

        refresh(
            fixtureUid = fixtureUid,
            endpoint = FixturePollingEndpoint.LINEUP,
            query = {
                matchDataQueryService
                    .getFixtureLineup(fixtureUid)
                    .map { domain -> matchDataMapper.toFixtureLineupResponse(domain) }
            },
            createDocument = { response: FixtureLineupResponse -> cacheDocumentFactory.create(response) },
        )

        refresh(
            fixtureUid = fixtureUid,
            endpoint = FixturePollingEndpoint.EVENTS,
            query = {
                matchDataQueryService
                    .getFixtureEvents(fixtureUid)
                    .map { domain -> matchDataMapper.toFixtureEventsResponse(domain) }
            },
            createDocument = { response: FixtureEventsResponse -> cacheDocumentFactory.create(response) },
        )

        refresh(
            fixtureUid = fixtureUid,
            endpoint = FixturePollingEndpoint.STATISTICS,
            query = {
                matchDataQueryService
                    .getFixtureStatistics(fixtureUid)
                    .map { domain -> matchDataMapper.toFixtureStatisticsResponse(domain) }
            },
            createDocument = { response: FixtureStatisticsResponse -> cacheDocumentFactory.create(response) },
        )
    }

    private fun <T : Any> refresh(
        fixtureUid: String,
        endpoint: FixturePollingEndpoint,
        query: () -> DomainResult<T, DomainFail>,
        createDocument: (T) -> FixtureResponseCacheDocument,
    ) {
        when (val result = query()) {
            is DomainResult.Success -> {
                val document = createDocument(result.value)
                cacheManager.save(fixtureUid, endpoint, document)
                log.info("Refreshed fixture cache snapshot. fixtureUid={}, endpoint={}", fixtureUid, endpoint)
            }
            is DomainResult.Fail -> {
                log.warn(
                    "Failed to refresh fixture cache snapshot. fixtureUid={}, endpoint={}, error={}",
                    fixtureUid,
                    endpoint,
                    result.error,
                )
            }
        }
    }
}
