package com.footballay.core.web.football.cache.refresh

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.model.match.FixtureEventsModel
import com.footballay.core.domain.model.match.FixtureLineupModel
import com.footballay.core.domain.model.match.FixtureLiveStatusModel
import com.footballay.core.domain.model.match.FixtureStatisticsModel
import com.footballay.core.infra.query.MatchDataQueryService
import com.footballay.core.web.football.cache.FixturePollingEndpoint
import com.footballay.core.web.football.cache.FixtureWebCacheManager
import com.footballay.core.web.football.cache.hash.FixtureResponseCacheDocument
import com.footballay.core.web.football.cache.hash.FixtureResponseCacheDocumentFactory
import com.footballay.core.web.football.dto.FixtureEventsResponse
import com.footballay.core.web.football.dto.FixtureLineupResponse
import com.footballay.core.web.football.dto.FixtureLiveStatusResponse
import com.footballay.core.web.football.dto.FixtureStatisticsResponse
import com.footballay.core.web.football.mapper.MatchDataMapper
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FixtureSnapshotRefreshServiceTest {
    private lateinit var matchDataQueryService: MatchDataQueryService
    private lateinit var matchDataMapper: MatchDataMapper
    private lateinit var cacheDocumentFactory: FixtureResponseCacheDocumentFactory
    private lateinit var cacheManager: FixtureWebCacheManager
    private lateinit var service: FixtureSnapshotRefreshService

    @BeforeEach
    fun setUp() {
        matchDataQueryService = mockk()
        matchDataMapper = mockk()
        cacheDocumentFactory = mockk()
        cacheManager = mockk()
        service =
            FixtureSnapshotRefreshService(
                matchDataQueryService = matchDataQueryService,
                matchDataMapper = matchDataMapper,
                cacheDocumentFactory = cacheDocumentFactory,
                cacheManager = cacheManager,
            )
    }

    @Test
    fun `refreshAll - polling 4개 endpoint snapshot 을 재생성하고 저장한다`() {
        val trigger = FixtureMatchCacheRefreshTrigger(fixtureUid = "fixture-1")

        val liveStatusModel = FixtureLiveStatusModel("fixture-1", FixtureLiveStatusModel.LiveStatus(12, "1H", "First Half", FixtureLiveStatusModel.Score(1, 0)))
        val liveStatusResponse = FixtureLiveStatusResponse("fixture-1", FixtureLiveStatusResponse.LiveStatus(12, "1H", "First Half", FixtureLiveStatusResponse.Score(1, 0)))
        val liveStatusDocument = FixtureResponseCacheDocument("""{"fixtureUid":"fixture-1","liveStatus":{"elapsed":12,"shortStatus":"1H","longStatus":"First Half","score":{"home":1,"away":0}}}""", "etag-status")

        val lineupModel = FixtureLineupModel("fixture-1", FixtureLineupModel.Lineup(home = null, away = null))
        val lineupResponse = FixtureLineupResponse("fixture-1", FixtureLineupResponse.Lineup(home = null, away = null))
        val lineupDocument = FixtureResponseCacheDocument("""{"fixtureUid":"fixture-1","lineup":{"home":null,"away":null}}""", "etag-lineup")

        val eventsModel = FixtureEventsModel("fixture-1", emptyList())
        val eventsResponse = FixtureEventsResponse("fixture-1", emptyList())
        val eventsDocument = FixtureResponseCacheDocument("""{"fixtureUid":"fixture-1","events":[]}""", "etag-events")

        val statisticsModel = FixtureStatisticsModel(FixtureStatisticsModel.FixtureBasic("fixture-1", 12, "1H"), null, null)
        val statisticsResponse = FixtureStatisticsResponse(FixtureStatisticsResponse.FixtureBasic("fixture-1", 12, "1H"), null, null)
        val statisticsDocument = FixtureResponseCacheDocument("""{"fixture":{"uid":"fixture-1","elapsed":12,"status":"1H"},"home":null,"away":null}""", "etag-statistics")

        every { matchDataQueryService.getFixtureLiveStatus("fixture-1") } returns DomainResult.Success(liveStatusModel)
        every { matchDataMapper.toFixtureLiveStatusResponse(liveStatusModel) } returns liveStatusResponse
        every { cacheDocumentFactory.create(liveStatusResponse) } returns liveStatusDocument

        every { matchDataQueryService.getFixtureLineup("fixture-1") } returns DomainResult.Success(lineupModel)
        every { matchDataMapper.toFixtureLineupResponse(lineupModel) } returns lineupResponse
        every { cacheDocumentFactory.create(lineupResponse) } returns lineupDocument

        every { matchDataQueryService.getFixtureEvents("fixture-1") } returns DomainResult.Success(eventsModel)
        every { matchDataMapper.toFixtureEventsResponse(eventsModel) } returns eventsResponse
        every { cacheDocumentFactory.create(eventsResponse) } returns eventsDocument

        every { matchDataQueryService.getFixtureStatistics("fixture-1") } returns DomainResult.Success(statisticsModel)
        every { matchDataMapper.toFixtureStatisticsResponse(statisticsModel) } returns statisticsResponse
        every { cacheDocumentFactory.create(statisticsResponse) } returns statisticsDocument

        every { cacheManager.save(any(), any(), any()) } just Runs

        service.refreshAll(trigger)

        verify { cacheManager.save("fixture-1", FixturePollingEndpoint.STATUS, liveStatusDocument) }
        verify { cacheManager.save("fixture-1", FixturePollingEndpoint.LINEUP, lineupDocument) }
        verify { cacheManager.save("fixture-1", FixturePollingEndpoint.EVENTS, eventsDocument) }
        verify { cacheManager.save("fixture-1", FixturePollingEndpoint.STATISTICS, statisticsDocument) }
    }

    @Test
    fun `refreshAll - 일부 endpoint 조회 실패가 나도 나머지 endpoint refresh 는 계속 진행한다`() {
        val trigger = FixtureMatchCacheRefreshTrigger(fixtureUid = "fixture-1")
        val error = DomainFail.NotFound(resource = "Fixture", id = "fixture-1")

        val lineupModel = FixtureLineupModel("fixture-1", FixtureLineupModel.Lineup(home = null, away = null))
        val lineupResponse = FixtureLineupResponse("fixture-1", FixtureLineupResponse.Lineup(home = null, away = null))
        val lineupDocument = FixtureResponseCacheDocument("""{"fixtureUid":"fixture-1","lineup":{"home":null,"away":null}}""", "etag-lineup")

        val eventsModel = FixtureEventsModel("fixture-1", emptyList())
        val eventsResponse = FixtureEventsResponse("fixture-1", emptyList())
        val eventsDocument = FixtureResponseCacheDocument("""{"fixtureUid":"fixture-1","events":[]}""", "etag-events")

        val statisticsModel = FixtureStatisticsModel(FixtureStatisticsModel.FixtureBasic("fixture-1", 12, "1H"), null, null)
        val statisticsResponse = FixtureStatisticsResponse(FixtureStatisticsResponse.FixtureBasic("fixture-1", 12, "1H"), null, null)
        val statisticsDocument = FixtureResponseCacheDocument("""{"fixture":{"uid":"fixture-1","elapsed":12,"status":"1H"},"home":null,"away":null}""", "etag-statistics")

        every { matchDataQueryService.getFixtureLiveStatus("fixture-1") } returns DomainResult.Fail(error)

        every { matchDataQueryService.getFixtureLineup("fixture-1") } returns DomainResult.Success(lineupModel)
        every { matchDataMapper.toFixtureLineupResponse(lineupModel) } returns lineupResponse
        every { cacheDocumentFactory.create(lineupResponse) } returns lineupDocument

        every { matchDataQueryService.getFixtureEvents("fixture-1") } returns DomainResult.Success(eventsModel)
        every { matchDataMapper.toFixtureEventsResponse(eventsModel) } returns eventsResponse
        every { cacheDocumentFactory.create(eventsResponse) } returns eventsDocument

        every { matchDataQueryService.getFixtureStatistics("fixture-1") } returns DomainResult.Success(statisticsModel)
        every { matchDataMapper.toFixtureStatisticsResponse(statisticsModel) } returns statisticsResponse
        every { cacheDocumentFactory.create(statisticsResponse) } returns statisticsDocument

        every { cacheManager.save(any(), any(), any()) } just Runs

        service.refreshAll(trigger)

        verify(exactly = 0) { cacheManager.save("fixture-1", FixturePollingEndpoint.STATUS, any()) }
        verify { cacheManager.save("fixture-1", FixturePollingEndpoint.LINEUP, lineupDocument) }
        verify { cacheManager.save("fixture-1", FixturePollingEndpoint.EVENTS, eventsDocument) }
        verify { cacheManager.save("fixture-1", FixturePollingEndpoint.STATISTICS, statisticsDocument) }
    }
}
