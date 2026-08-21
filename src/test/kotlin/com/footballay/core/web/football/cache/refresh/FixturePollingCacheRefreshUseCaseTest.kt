package com.footballay.core.web.football.cache.refresh

import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.model.match.FixtureEventsModel
import com.footballay.core.domain.model.match.FixtureLineupModel
import com.footballay.core.domain.model.match.FixtureLiveStatusModel
import com.footballay.core.domain.model.match.FixtureStatisticsModel
import com.footballay.core.infra.query.MatchDataQueryService
import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.football.cache.FixturePollingEndpoint
import com.footballay.core.web.football.cache.FixtureWebCacheIdentity
import com.footballay.core.web.football.cache.FixtureWebCacheManager
import com.footballay.core.web.football.cache.hash.FixtureResponseCacheDocument
import com.footballay.core.web.football.cache.hash.FixtureResponseCacheDocumentFactory
import com.footballay.core.web.football.dto.FixtureEventsResponse
import com.footballay.core.web.football.dto.FixtureLineupResponse
import com.footballay.core.web.football.dto.FixtureLiveStatusResponse
import com.footballay.core.web.football.dto.FixtureStatisticsResponse
import com.footballay.core.web.football.localization.FootballResponseLocalizationService
import com.footballay.core.web.football.localization.LocalizedFixtureEventsModel
import com.footballay.core.web.football.localization.LocalizedFixtureLineupModel
import com.footballay.core.web.football.localization.LocalizedFixturePollingModels
import com.footballay.core.web.football.localization.LocalizedFixtureStatisticsModel
import com.footballay.core.web.football.mapper.MatchDataMapper
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FixturePollingCacheRefreshUseCaseTest {
    private lateinit var matchDataQueryService: MatchDataQueryService
    private lateinit var matchDataMapper: MatchDataMapper
    private lateinit var localizationService: FootballResponseLocalizationService
    private lateinit var cacheDocumentFactory: FixtureResponseCacheDocumentFactory
    private lateinit var cacheManager: FixtureWebCacheManager
    private lateinit var service: FixturePollingCacheRefreshUseCase

    @BeforeEach
    fun setUp() {
        matchDataQueryService = mockk()
        matchDataMapper = mockk()
        localizationService = mockk()
        cacheDocumentFactory = mockk()
        cacheManager = mockk()
        service =
            FixturePollingCacheRefreshUseCase(
                matchDataQueryService = matchDataQueryService,
                matchDataMapper = matchDataMapper,
                localizationService = localizationService,
                cacheDocumentFactory = cacheDocumentFactory,
                cacheManager = cacheManager,
            )
    }

    @Test
    fun `refreshAll - status 하나와 locale별 localized snapshot을 저장한다`() {
        val models = pollingModels()
        val statusResponse = mockk<FixtureLiveStatusResponse>()
        val statusDocument = FixtureResponseCacheDocument("status", "etag-status")
        every { matchDataQueryService.getFixtureLiveStatus("fixture-1") } returns DomainResult.Success(models.liveStatus)
        every { matchDataQueryService.getFixtureLineup("fixture-1") } returns DomainResult.Success(models.lineup)
        every { matchDataQueryService.getFixtureEvents("fixture-1") } returns DomainResult.Success(models.events)
        every { matchDataQueryService.getFixtureStatistics("fixture-1") } returns DomainResult.Success(models.statistics)
        every { matchDataMapper.toFixtureLiveStatusResponse(models.liveStatus) } returns statusResponse
        every { cacheDocumentFactory.create(statusResponse) } returns statusDocument
        every {
            localizationService.preparePollingModels(models.lineup, models.events, models.statistics, SupportedLocale.entries)
        } returns models.localizedByLocale
        every { matchDataMapper.toFixtureLineupResponse(any<LocalizedFixtureLineupModel>()) } returns FixtureLineupResponse("fixture-1", FixtureLineupResponse.Lineup(null, null))
        every { matchDataMapper.toFixtureEventsResponse(any<LocalizedFixtureEventsModel>()) } returns FixtureEventsResponse("fixture-1", emptyList())
        every { matchDataMapper.toFixtureStatisticsResponse(any<LocalizedFixtureStatisticsModel>()) } returns FixtureStatisticsResponse(FixtureStatisticsResponse.FixtureBasic("fixture-1", null, "1H"), null, null)
        every { cacheDocumentFactory.create(any<FixtureLineupResponse>()) } returns FixtureResponseCacheDocument("lineup", "etag-lineup")
        every { cacheDocumentFactory.create(any<FixtureEventsResponse>()) } returns FixtureResponseCacheDocument("events", "etag-events")
        every { cacheDocumentFactory.create(any<FixtureStatisticsResponse>()) } returns FixtureResponseCacheDocument("statistics", "etag-statistics")
        every { cacheManager.save(any(), any()) } just runs

        service.refreshAll(FixtureMatchCacheRefreshTrigger(fixtureUid = "fixture-1"))

        verify(exactly = 1) { matchDataQueryService.getFixtureLiveStatus("fixture-1") }
        verify(exactly = 1) { matchDataQueryService.getFixtureLineup("fixture-1") }
        verify(exactly = 1) { matchDataQueryService.getFixtureEvents("fixture-1") }
        verify(exactly = 1) { matchDataQueryService.getFixtureStatistics("fixture-1") }
        verify { cacheManager.save(FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.STATUS, null), statusDocument) }
        for (locale in SupportedLocale.entries) {
            verify { cacheManager.save(FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.LINEUP, locale), any()) }
            verify { cacheManager.save(FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.EVENTS, locale), any()) }
            verify { cacheManager.save(FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.STATISTICS, locale), any()) }
        }
        verify(exactly = 0) { cacheManager.save(FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.LINEUP, null), any()) }
        verify(exactly = 0) { cacheManager.save(FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.EVENTS, null), any()) }
        verify(exactly = 0) { cacheManager.save(FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.STATISTICS, null), any()) }
    }

    @Test
    fun `refreshAll - localization preparation 실패에도 status 저장은 유지한다`() {
        val models = pollingModels()
        val statusResponse = mockk<FixtureLiveStatusResponse>()
        val statusDocument = FixtureResponseCacheDocument("status", "etag-status")
        every { matchDataQueryService.getFixtureLiveStatus("fixture-1") } returns DomainResult.Success(models.liveStatus)
        every { matchDataQueryService.getFixtureLineup("fixture-1") } returns DomainResult.Success(models.lineup)
        every { matchDataQueryService.getFixtureEvents("fixture-1") } returns DomainResult.Success(models.events)
        every { matchDataQueryService.getFixtureStatistics("fixture-1") } returns DomainResult.Success(models.statistics)
        every { matchDataMapper.toFixtureLiveStatusResponse(models.liveStatus) } returns statusResponse
        every { cacheDocumentFactory.create(statusResponse) } returns statusDocument
        every {
            localizationService.preparePollingModels(models.lineup, models.events, models.statistics, SupportedLocale.entries)
        } throws IllegalStateException("localization failed")
        every { cacheManager.save(any(), any()) } just runs

        service.refreshAll(FixtureMatchCacheRefreshTrigger(fixtureUid = "fixture-1"))

        verify { cacheManager.save(FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.STATUS, null), statusDocument) }
        verify(exactly = 0) { cacheManager.save(match { it.locale != null }, any()) }
    }

    private fun pollingModels(): PollingModels {
        val localized =
            LocalizedFixturePollingModels(
                lineup = mockk(),
                events = mockk(),
                statistics = mockk(),
            )

        return PollingModels(
            liveStatus = mockk<FixtureLiveStatusModel>(),
            lineup = mockk<FixtureLineupModel>(),
            events = mockk<FixtureEventsModel>(),
            statistics = mockk<FixtureStatisticsModel>(),
            localizedByLocale = SupportedLocale.entries.associateWith { localized },
        )
    }

    private data class PollingModels(
        val liveStatus: FixtureLiveStatusModel,
        val lineup: FixtureLineupModel,
        val events: FixtureEventsModel,
        val statistics: FixtureStatisticsModel,
        val localizedByLocale: Map<SupportedLocale, LocalizedFixturePollingModels>,
    )
}
