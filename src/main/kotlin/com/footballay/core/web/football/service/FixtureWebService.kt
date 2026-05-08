package com.footballay.core.web.football.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.common.result.map
import com.footballay.core.common.logging.logger
import com.footballay.core.cache.matchdata.polling.FixturePollingEndpoint
import com.footballay.core.cache.matchdata.polling.MatchDataPollingCacheManager
import com.footballay.core.cache.matchdata.polling.hash.FixtureHttpEtagHelper
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
    private val pollingCacheManager: MatchDataPollingCacheManager,
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
        )
    }

    private fun getPollingFixture(
        fixtureUid: String,
        endpoint: FixturePollingEndpoint,
        ifNoneMatch: String?,
        bypassCacheRead: Boolean,
    ): FixtureWebResult {
        if (!bypassCacheRead) {
            if (!ifNoneMatch.isNullOrBlank()) {
                val cachedEtagHash = pollingCacheManager.findEtagHash(fixtureUid, endpoint)
                if (cachedEtagHash != null && httpEtagHelper.matchesIfNoneMatch(ifNoneMatch, cachedEtagHash)) {
                    return FixtureWebResult.NotModified(cachedEtagHash)
                }
            }

            val cached = pollingCacheManager.findSnapshot(fixtureUid, endpoint)
            if (cached != null) {
                return FixtureWebResult.Ok(cached.snapshotJson, cached.etagHash)
            }
        }

        return when (val result = pollingCacheManager.refreshEndpoint(fixtureUid, endpoint)) {
            is DomainResult.Success -> {
                val document = result.value
                FixtureWebResult.Ok(document.snapshotJson, document.etagHash)
            }
            is DomainResult.Fail -> FixtureWebResult.Fail(result.error)
        }
    }
}
