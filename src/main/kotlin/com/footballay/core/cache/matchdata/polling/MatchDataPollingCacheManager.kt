package com.footballay.core.cache.matchdata.polling

import com.footballay.core.common.logging.logger
import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.common.result.map
import com.footballay.core.cache.matchdata.polling.hash.FixtureResponseCacheDocument
import com.footballay.core.cache.matchdata.polling.hash.FixtureResponseCacheDocumentFactory
import com.footballay.core.matchdata.facade.MatchDataFacade
import com.footballay.core.web.football.mapper.MatchDataMapper
import org.springframework.stereotype.Service

interface MatchDataPollingCacheManager {
    fun findSnapshot(
        fixtureUid: String,
        endpoint: FixturePollingEndpoint,
    ): FixtureWebCacheSnapshot?

    fun findEtagHash(
        fixtureUid: String,
        endpoint: FixturePollingEndpoint,
    ): String?

    fun refreshEndpoint(
        fixtureUid: String,
        endpoint: FixturePollingEndpoint,
    ): DomainResult<FixtureResponseCacheDocument, DomainFail>

    fun refreshFixture(
        fixtureUid: String,
        source: String? = null,
        jobPhase: String? = null,
    )
}

@Service
class DefaultMatchDataPollingCacheManager(
    private val matchDataFacade: MatchDataFacade,
    private val matchDataMapper: MatchDataMapper,
    private val cacheDocumentFactory: FixtureResponseCacheDocumentFactory,
    private val fixtureWebCacheManager: FixtureWebCacheManager,
) : MatchDataPollingCacheManager {
    private val log = logger()

    override fun findSnapshot(
        fixtureUid: String,
        endpoint: FixturePollingEndpoint,
    ): FixtureWebCacheSnapshot? = fixtureWebCacheManager.findSnapshot(fixtureUid, endpoint)

    override fun findEtagHash(
        fixtureUid: String,
        endpoint: FixturePollingEndpoint,
    ): String? = fixtureWebCacheManager.findEtagHash(fixtureUid, endpoint)

    override fun refreshEndpoint(
        fixtureUid: String,
        endpoint: FixturePollingEndpoint,
    ): DomainResult<FixtureResponseCacheDocument, DomainFail> {
        val result =
            when (endpoint) {
                FixturePollingEndpoint.STATUS ->
                    matchDataFacade
                        .getFixtureLiveStatus(fixtureUid)
                        .map { domain -> matchDataMapper.toFixtureLiveStatusResponse(domain) }
                        .map { response -> cacheDocumentFactory.create(response) }

                FixturePollingEndpoint.LINEUP ->
                    matchDataFacade
                        .getFixtureLineup(fixtureUid)
                        .map { domain -> matchDataMapper.toFixtureLineupResponse(domain) }
                        .map { response -> cacheDocumentFactory.create(response) }

                FixturePollingEndpoint.EVENTS ->
                    matchDataFacade
                        .getFixtureEvents(fixtureUid)
                        .map { domain -> matchDataMapper.toFixtureEventsResponse(domain) }
                        .map { response -> cacheDocumentFactory.create(response) }

                FixturePollingEndpoint.STATISTICS ->
                    matchDataFacade
                        .getFixtureStatistics(fixtureUid)
                        .map { domain -> matchDataMapper.toFixtureStatisticsResponse(domain) }
                        .map { response -> cacheDocumentFactory.create(response) }
            }

        if (result is DomainResult.Success) {
            fixtureWebCacheManager.save(fixtureUid, endpoint, result.value)
        }

        return result
    }

    override fun refreshFixture(
        fixtureUid: String,
        source: String?,
        jobPhase: String?,
    ) {
        pollingEndpoints.forEach { endpoint ->
            when (val result = refreshEndpoint(fixtureUid, endpoint)) {
                is DomainResult.Success ->
                    log.info(
                        "Refreshed match data polling cache. fixtureUid={}, endpoint={}, source={}, jobPhase={}",
                        fixtureUid,
                        endpoint,
                        source,
                        jobPhase,
                    )

                is DomainResult.Fail ->
                    log.warn(
                        "Failed to refresh match data polling cache. fixtureUid={}, endpoint={}, source={}, jobPhase={}, error={}",
                        fixtureUid,
                        endpoint,
                        source,
                        jobPhase,
                        result.error,
                    )
            }
        }
    }

    private companion object {
        val pollingEndpoints =
            listOf(
                FixturePollingEndpoint.STATUS,
                FixturePollingEndpoint.LINEUP,
                FixturePollingEndpoint.EVENTS,
                FixturePollingEndpoint.STATISTICS,
            )
    }
}
