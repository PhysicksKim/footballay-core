package com.footballay.core.web.football.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.common.result.map
import com.footballay.core.infra.query.MatchDataQueryService
import com.footballay.core.logger
import com.footballay.core.web.football.dto.*
import com.footballay.core.web.football.mapper.MatchDataMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Footballay Fixture Web Service
 */
@Service
@Transactional(readOnly = true)
class FixtureWebService(
    private val matchDataQueryService: MatchDataQueryService,
    private val matchDataMapper: MatchDataMapper,
) {
    private val log = logger()

    fun getFixtureInfo(fixtureUid: String): DomainResult<FixtureInfoResponse, DomainFail> {
        log.info("getFixtureInfo. fixtureUid={}", fixtureUid)

        return matchDataQueryService
            .getFixtureInfo(fixtureUid)
            .map { domain -> matchDataMapper.toFixtureInfoResponse(domain) }
    }

    fun getFixtureLiveStatus(fixtureUid: String): DomainResult<FixtureLiveStatusResponse, DomainFail> {
        log.info("getFixtureLiveStatus. fixtureUid={}", fixtureUid)

        return matchDataQueryService
            .getFixtureLiveStatus(fixtureUid)
            .map { domain -> matchDataMapper.toFixtureLiveStatusResponse(domain) }
    }

    fun getFixtureEvents(fixtureUid: String): DomainResult<FixtureEventsResponse, DomainFail> {
        log.info("getFixtureEvents. fixtureUid={}", fixtureUid)

        return matchDataQueryService
            .getFixtureEvents(fixtureUid)
            .map { domain -> matchDataMapper.toFixtureEventsResponse(domain) }
    }

    fun getFixtureLineup(fixtureUid: String): DomainResult<FixtureLineupResponse, DomainFail> {
        log.info("getFixtureLineup. fixtureUid={}", fixtureUid)

        return matchDataQueryService
            .getFixtureLineup(fixtureUid)
            .map { domain -> matchDataMapper.toFixtureLineupResponse(domain) }
    }

    fun getFixtureStatistics(fixtureUid: String): DomainResult<FixtureStatisticsResponse, DomainFail> {
        log.info("getFixtureStatistics. fixtureUid={}", fixtureUid)

        return matchDataQueryService
            .getFixtureStatistics(fixtureUid)
            .map { domain -> matchDataMapper.toFixtureStatisticsResponse(domain) }
    }
}
