package com.footballay.core.web.football.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.common.result.map
import com.footballay.core.common.logging.logger
import com.footballay.core.matchdata.cache.FixturePollingEndpoint
import com.footballay.core.matchdata.cache.FixtureWebCacheManager
import com.footballay.core.matchdata.cache.hash.FixtureHttpEtagHelper
import com.footballay.core.matchdata.cache.hash.FixtureResponseCacheDocument
import com.footballay.core.matchdata.cache.hash.FixtureResponseCacheDocumentFactory
import com.footballay.core.matchdata.facade.MatchDataFacade
import com.footballay.core.web.football.dto.*
import com.footballay.core.web.football.mapper.MatchDataMapper
import org.springframework.stereotype.Service

/**
 * Footballay Fixture Web Service
 */
@Service
class FixtureWebService(
    private val matchDataFacade: MatchDataFacade,
    private val matchDataMapper: MatchDataMapper,
    private val cacheManager: FixtureWebCacheManager,
    private val cacheDocumentFactory: FixtureResponseCacheDocumentFactory,
    private val httpEtagHelper: FixtureHttpEtagHelper,
) {
    private val log = logger()

    fun getFixtureInfo(fixtureUid: String): DomainResult<FixtureInfoResponse, DomainFail> {
        log.info("getFixtureInfo. fixtureUid={}", fixtureUid)

        return matchDataFacade
            .getFixtureInfo(fixtureUid)
            .map { domain -> matchDataMapper.toFixtureInfoResponse(domain) }
    }

    fun getFixtureLiveStatus(
        fixtureUid: String,
        ifNoneMatch: String?,
        bypassCacheRead: Boolean = false,
    ): FixtureWebResult {
        log.info("getFixtureLiveStatus. fixtureUid={}", fixtureUid)

        return getPollingFixture(
            fixtureUid = fixtureUid,
            endpoint = FixturePollingEndpoint.STATUS,
            ifNoneMatch = ifNoneMatch,
            bypassCacheRead = bypassCacheRead,
            query = { queryFixtureLiveStatus(fixtureUid) },
            createDocument = { cacheDocumentFactory.create(it) },
        )
    }

    fun getFixtureEvents(
        fixtureUid: String,
        ifNoneMatch: String?,
        bypassCacheRead: Boolean = false,
    ): FixtureWebResult {
        log.info("getFixtureEvents. fixtureUid={}", fixtureUid)

        return getPollingFixture(
            fixtureUid = fixtureUid,
            endpoint = FixturePollingEndpoint.EVENTS,
            ifNoneMatch = ifNoneMatch,
            bypassCacheRead = bypassCacheRead,
            query = { queryFixtureEvents(fixtureUid) },
            createDocument = { cacheDocumentFactory.create(it) },
        )
    }

    fun getFixtureLineup(
        fixtureUid: String,
        ifNoneMatch: String?,
        bypassCacheRead: Boolean = false,
    ): FixtureWebResult {
        log.info("getFixtureLineup. fixtureUid={}", fixtureUid)

        return getPollingFixture(
            fixtureUid = fixtureUid,
            endpoint = FixturePollingEndpoint.LINEUP,
            ifNoneMatch = ifNoneMatch,
            bypassCacheRead = bypassCacheRead,
            query = { queryFixtureLineup(fixtureUid) },
            createDocument = { cacheDocumentFactory.create(it) },
        )
    }

    fun getFixtureStatistics(
        fixtureUid: String,
        ifNoneMatch: String?,
        bypassCacheRead: Boolean = false,
    ): FixtureWebResult {
        log.info("getFixtureStatistics. fixtureUid={}", fixtureUid)

        return getPollingFixture(
            fixtureUid = fixtureUid,
            endpoint = FixturePollingEndpoint.STATISTICS,
            ifNoneMatch = ifNoneMatch,
            bypassCacheRead = bypassCacheRead,
            query = { queryFixtureStatistics(fixtureUid) },
            createDocument = { cacheDocumentFactory.create(it) },
        )
    }

    private fun queryFixtureLiveStatus(fixtureUid: String): DomainResult<FixtureLiveStatusResponse, DomainFail> =
        matchDataFacade
            .getFixtureLiveStatus(fixtureUid)
            .map { domain -> matchDataMapper.toFixtureLiveStatusResponse(domain) }

    private fun queryFixtureEvents(fixtureUid: String): DomainResult<FixtureEventsResponse, DomainFail> =
        matchDataFacade
            .getFixtureEvents(fixtureUid)
            .map { domain -> matchDataMapper.toFixtureEventsResponse(domain) }

    private fun queryFixtureLineup(fixtureUid: String): DomainResult<FixtureLineupResponse, DomainFail> =
        matchDataFacade
            .getFixtureLineup(fixtureUid)
            .map { domain -> matchDataMapper.toFixtureLineupResponse(domain) }

    private fun queryFixtureStatistics(fixtureUid: String): DomainResult<FixtureStatisticsResponse, DomainFail> =
        matchDataFacade
            .getFixtureStatistics(fixtureUid)
            .map { domain -> matchDataMapper.toFixtureStatisticsResponse(domain) }

    private fun <T : Any> getPollingFixture(
        fixtureUid: String,
        endpoint: FixturePollingEndpoint,
        ifNoneMatch: String?,
        bypassCacheRead: Boolean,
        query: () -> DomainResult<T, DomainFail>,
        createDocument: (T) -> FixtureResponseCacheDocument,
    ): FixtureWebResult {
        if (!bypassCacheRead) {
            if (!ifNoneMatch.isNullOrBlank()) {
                val cachedEtagHash = cacheManager.findEtagHash(fixtureUid, endpoint)
                if (cachedEtagHash != null && httpEtagHelper.matchesIfNoneMatch(ifNoneMatch, cachedEtagHash)) {
                    return FixtureWebResult.NotModified(cachedEtagHash)
                }
            }

            val cached = cacheManager.findSnapshot(fixtureUid, endpoint)
            if (cached != null) {
                return FixtureWebResult.Ok(cached.snapshotJson, cached.etagHash)
            }
        }

        return when (val result = query()) {
            is DomainResult.Success -> {
                val document = createDocument(result.value)
                cacheManager.save(fixtureUid, endpoint, document)
                FixtureWebResult.Ok(document.snapshotJson, document.etagHash)
            }
            is DomainResult.Fail -> FixtureWebResult.Fail(result.error)
        }
    }
}
