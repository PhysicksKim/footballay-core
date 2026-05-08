package com.footballay.core.cache.matchdata.polling

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.football.match.FixtureLineupModel
import com.footballay.core.domain.football.match.FixtureLiveStatusModel
import com.footballay.core.cache.matchdata.polling.hash.FixtureResponseCacheDocument
import com.footballay.core.cache.matchdata.polling.hash.FixtureResponseCacheDocumentFactory
import com.footballay.core.matchdata.facade.MatchDataFacade
import com.footballay.core.web.football.dto.FixtureLineupResponse
import com.footballay.core.web.football.dto.FixtureLiveStatusResponse
import com.footballay.core.web.football.mapper.MatchDataMapper
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultMatchDataPollingCacheManagerTest {
    private lateinit var matchDataFacade: MatchDataFacade
    private lateinit var matchDataMapper: MatchDataMapper
    private lateinit var cacheDocumentFactory: FixtureResponseCacheDocumentFactory
    private lateinit var fixtureWebCacheManager: FixtureWebCacheManager
    private lateinit var manager: MatchDataPollingCacheManager

    @BeforeEach
    fun setUp() {
        matchDataFacade = mockk()
        matchDataMapper = mockk()
        cacheDocumentFactory = mockk()
        fixtureWebCacheManager = mockk()
        manager =
            DefaultMatchDataPollingCacheManager(
                matchDataFacade = matchDataFacade,
                matchDataMapper = matchDataMapper,
                cacheDocumentFactory = cacheDocumentFactory,
                fixtureWebCacheManager = fixtureWebCacheManager,
            )
    }

    @Test
    fun `refreshEndpoint - endpoint snapshot 을 생성하고 저장한다`() {
        val model =
            FixtureLiveStatusModel(
                fixtureUid = "fixture-1",
                liveStatus =
                    FixtureLiveStatusModel.LiveStatus(
                        elapsed = 17,
                        shortStatus = "1H",
                        longStatus = "First Half",
                        score = FixtureLiveStatusModel.Score(home = 1, away = 0),
                    ),
            )
        val response =
            FixtureLiveStatusResponse(
                fixtureUid = "fixture-1",
                liveStatus =
                    FixtureLiveStatusResponse.LiveStatus(
                        elapsed = 17,
                        shortStatus = "1H",
                        longStatus = "First Half",
                        score = FixtureLiveStatusResponse.Score(home = 1, away = 0),
                    ),
            )
        val document = FixtureResponseCacheDocument("""{"fixtureUid":"fixture-1"}""", "etag-status")

        every { matchDataFacade.getFixtureLiveStatus("fixture-1") } returns DomainResult.Success(model)
        every { matchDataMapper.toFixtureLiveStatusResponse(model) } returns response
        every { cacheDocumentFactory.create(response) } returns document
        every { fixtureWebCacheManager.save("fixture-1", FixturePollingEndpoint.STATUS, document) } just Runs

        val result = manager.refreshEndpoint("fixture-1", FixturePollingEndpoint.STATUS)

        assertThat(result).isEqualTo(DomainResult.Success(document))
        verify { fixtureWebCacheManager.save("fixture-1", FixturePollingEndpoint.STATUS, document) }
    }

    @Test
    fun `refreshEndpoint - 조회 실패면 저장하지 않고 실패를 반환한다`() {
        val error = DomainFail.NotFound(resource = "Fixture", id = "missing")

        every { matchDataFacade.getFixtureLiveStatus("missing") } returns DomainResult.Fail(error)

        val result = manager.refreshEndpoint("missing", FixturePollingEndpoint.STATUS)

        assertThat(result).isEqualTo(DomainResult.Fail(error))
        verify(exactly = 0) { fixtureWebCacheManager.save(any(), any(), any()) }
    }

    @Test
    fun `refreshFixture - 일부 endpoint 실패가 나도 나머지 refresh 는 계속 진행한다`() {
        val error = DomainFail.NotFound(resource = "Fixture", id = "fixture-1")
        val lineupModel = FixtureLineupModel("fixture-1", FixtureLineupModel.Lineup(home = null, away = null))
        val lineupResponse = FixtureLineupResponse("fixture-1", FixtureLineupResponse.Lineup(home = null, away = null))
        val lineupDocument = FixtureResponseCacheDocument("""{"fixtureUid":"fixture-1","lineup":{"home":null,"away":null}}""", "etag-lineup")

        every { matchDataFacade.getFixtureLiveStatus("fixture-1") } returns DomainResult.Fail(error)
        every { matchDataFacade.getFixtureLineup("fixture-1") } returns DomainResult.Success(lineupModel)
        every { matchDataMapper.toFixtureLineupResponse(lineupModel) } returns lineupResponse
        every { cacheDocumentFactory.create(lineupResponse) } returns lineupDocument
        every { matchDataFacade.getFixtureEvents("fixture-1") } returns DomainResult.Fail(error)
        every { matchDataFacade.getFixtureStatistics("fixture-1") } returns DomainResult.Fail(error)
        every { fixtureWebCacheManager.save(any(), any(), any()) } just Runs

        manager.refreshFixture("fixture-1", source = "MATCH_DATA_SYNC", jobPhase = "LIVE_MATCH")

        verify(exactly = 0) { fixtureWebCacheManager.save("fixture-1", FixturePollingEndpoint.STATUS, any()) }
        verify { fixtureWebCacheManager.save("fixture-1", FixturePollingEndpoint.LINEUP, lineupDocument) }
        verify(exactly = 0) { fixtureWebCacheManager.save("fixture-1", FixturePollingEndpoint.EVENTS, any()) }
        verify(exactly = 0) { fixtureWebCacheManager.save("fixture-1", FixturePollingEndpoint.STATISTICS, any()) }
    }
}
