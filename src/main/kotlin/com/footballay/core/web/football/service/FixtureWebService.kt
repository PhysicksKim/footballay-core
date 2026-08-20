package com.footballay.core.web.football.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.common.result.map
import com.footballay.core.infra.query.MatchDataQueryService
import com.footballay.core.logger
import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.football.cache.FixturePollingEndpoint
import com.footballay.core.web.football.cache.FixtureWebCacheManager
import com.footballay.core.web.football.cache.hash.FixtureHttpEtagHelper
import com.footballay.core.web.football.cache.hash.FixtureResponseCacheDocument
import com.footballay.core.web.football.cache.hash.FixtureResponseCacheDocumentFactory
import com.footballay.core.web.football.dto.*
import com.footballay.core.web.football.localization.FootballResponseLocalizationService
import com.footballay.core.web.football.mapper.MatchDataMapper
import org.springframework.stereotype.Service

/**
 * Footballay Fixture Web Service
 */
@Service
class FixtureWebService(
    private val matchDataQueryService: MatchDataQueryService,
    private val matchDataMapper: MatchDataMapper,
    private val localizationService: FootballResponseLocalizationService,
    private val cacheManager: FixtureWebCacheManager,
    private val cacheDocumentFactory: FixtureResponseCacheDocumentFactory,
    private val httpEtagHelper: FixtureHttpEtagHelper,
) {
    private val log = logger()

    fun getFixtureInfo(
        fixtureUid: String,
        locale: SupportedLocale = SupportedLocale.EN,
    ): DomainResult<FixtureInfoResponse, DomainFail> {
        log.info("getFixtureInfo. fixtureUid={}", fixtureUid)

        return matchDataQueryService
            .getFixtureInfo(fixtureUid)
            .map { localizationService.localizeFixtureInfo(it, locale) }
            .map(matchDataMapper::toFixtureInfoResponse)
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
        locale: SupportedLocale = SupportedLocale.EN,
    ): FixtureWebResult {
        log.info("getFixtureEvents. fixtureUid={}", fixtureUid)

        return getPollingFixture(
            fixtureUid = fixtureUid,
            endpoint = FixturePollingEndpoint.EVENTS,
            ifNoneMatch = ifNoneMatch,
            bypassCacheRead = bypassCacheRead,
            query = { queryFixtureEvents(fixtureUid, locale) },
            createDocument = { cacheDocumentFactory.create(it) },
        )
    }

    fun getFixtureLineup(
        fixtureUid: String,
        ifNoneMatch: String?,
        bypassCacheRead: Boolean = false,
        locale: SupportedLocale = SupportedLocale.EN,
    ): FixtureWebResult {
        log.info("getFixtureLineup. fixtureUid={}", fixtureUid)

        return getPollingFixture(
            fixtureUid = fixtureUid,
            endpoint = FixturePollingEndpoint.LINEUP,
            ifNoneMatch = ifNoneMatch,
            bypassCacheRead = bypassCacheRead,
            query = { queryFixtureLineup(fixtureUid, locale) },
            createDocument = { cacheDocumentFactory.create(it) },
        )
    }

    fun getFixtureStatistics(
        fixtureUid: String,
        ifNoneMatch: String?,
        bypassCacheRead: Boolean = false,
        locale: SupportedLocale = SupportedLocale.EN,
    ): FixtureWebResult {
        log.info("getFixtureStatistics. fixtureUid={}", fixtureUid)

        return getPollingFixture(
            fixtureUid = fixtureUid,
            endpoint = FixturePollingEndpoint.STATISTICS,
            ifNoneMatch = ifNoneMatch,
            bypassCacheRead = bypassCacheRead,
            query = { queryFixtureStatistics(fixtureUid, locale) },
            createDocument = { cacheDocumentFactory.create(it) },
        )
    }

    private fun queryFixtureLiveStatus(fixtureUid: String): DomainResult<FixtureLiveStatusResponse, DomainFail> =
        matchDataQueryService
            .getFixtureLiveStatus(fixtureUid)
            .map { model -> matchDataMapper.toFixtureLiveStatusResponse(model) }

    private fun queryFixtureEvents(
        fixtureUid: String,
        locale: SupportedLocale,
    ): DomainResult<FixtureEventsResponse, DomainFail> =
        matchDataQueryService
            .getFixtureEvents(fixtureUid)
            .map { localizationService.localizeEvents(it, locale) }
            .map(matchDataMapper::toFixtureEventsResponse)

    private fun queryFixtureLineup(
        fixtureUid: String,
        locale: SupportedLocale,
    ): DomainResult<FixtureLineupResponse, DomainFail> =
        matchDataQueryService
            .getFixtureLineup(fixtureUid)
            .map { localizationService.localizeLineup(it, locale) }
            .map(matchDataMapper::toFixtureLineupResponse)

    private fun queryFixtureStatistics(
        fixtureUid: String,
        locale: SupportedLocale,
    ): DomainResult<FixtureStatisticsResponse, DomainFail> =
        matchDataQueryService
            .getFixtureStatistics(fixtureUid)
            .map { localizationService.localizeStatistics(it, locale) }
            .map(matchDataMapper::toFixtureStatisticsResponse)

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
