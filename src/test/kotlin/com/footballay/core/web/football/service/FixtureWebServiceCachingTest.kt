package com.footballay.core.web.football.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.football.match.FixtureLiveStatusModel
import com.footballay.core.matchdata.read.MatchDataQueryService
import com.footballay.core.matchdata.cache.FixturePollingEndpoint
import com.footballay.core.matchdata.cache.FixtureWebCacheManager
import com.footballay.core.matchdata.cache.FixtureWebCacheSnapshot
import com.footballay.core.matchdata.cache.hash.FixtureHttpEtagHelper
import com.footballay.core.matchdata.cache.hash.FixtureResponseCacheDocument
import com.footballay.core.matchdata.cache.hash.FixtureResponseCacheDocumentFactory
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

class FixtureWebServiceCachingTest {
    private lateinit var matchDataQueryService: MatchDataQueryService
    private lateinit var matchDataMapper: MatchDataMapper
    private lateinit var cacheManager: FixtureWebCacheManager
    private lateinit var cacheDocumentFactory: FixtureResponseCacheDocumentFactory
    private lateinit var httpEtagHelper: FixtureHttpEtagHelper
    private lateinit var service: FixtureWebService

    @BeforeEach
    fun setUp() {
        matchDataQueryService = mockk()
        matchDataMapper = mockk()
        cacheManager = mockk()
        cacheDocumentFactory = mockk()
        httpEtagHelper = mockk()
        service =
            FixtureWebService(
                matchDataQueryService = matchDataQueryService,
                matchDataMapper = matchDataMapper,
                cacheManager = cacheManager,
                cacheDocumentFactory = cacheDocumentFactory,
                httpEtagHelper = httpEtagHelper,
            )
    }

    @Test
    fun `getFixtureLiveStatus - cache hit 이고 etag 가 같으면 NotModified 를 반환한다`() {
        every { cacheManager.findEtagHash("fixture-1", FixturePollingEndpoint.STATUS) } returns "etag-1"
        every { httpEtagHelper.matchesIfNoneMatch("""W/"etag-1"""", "etag-1") } returns true

        val result = service.getFixtureLiveStatus("fixture-1", """W/"etag-1"""")

        assertThat(result).isEqualTo(FixtureWebResult.NotModified("etag-1"))
        verify(exactly = 0) { cacheManager.findSnapshot(any(), any()) }
        verify(exactly = 0) { matchDataQueryService.getFixtureLiveStatus(any()) }
    }

    @Test
    fun `getFixtureLiveStatus - cache miss 면 조회 후 저장하고 Ok 를 반환한다`() {
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
        val document =
            FixtureResponseCacheDocument(
                snapshotJson = """{"fixtureUid":"fixture-1","liveStatus":{"elapsed":17,"shortStatus":"1H","longStatus":"First Half","score":{"home":1,"away":0}}}""",
                etagHash = "etag-2",
            )

        every { cacheManager.findSnapshot("fixture-1", FixturePollingEndpoint.STATUS) } returns null
        every { matchDataQueryService.getFixtureLiveStatus("fixture-1") } returns DomainResult.Success(model)
        every { matchDataMapper.toFixtureLiveStatusResponse(model) } returns response
        every { cacheDocumentFactory.create(response) } returns document
        every { cacheManager.save("fixture-1", FixturePollingEndpoint.STATUS, document) } just Runs

        val result = service.getFixtureLiveStatus("fixture-1", null)

        assertThat(result).isEqualTo(FixtureWebResult.Ok(document.snapshotJson, document.etagHash))
        verify { cacheManager.save("fixture-1", FixturePollingEndpoint.STATUS, document) }
    }

    @Test
    fun `getFixtureLiveStatus - bypass cache read 이면 캐시가 있어도 원본 조회를 강제한다`() {
        val model =
            FixtureLiveStatusModel(
                fixtureUid = "fixture-1",
                liveStatus =
                    FixtureLiveStatusModel.LiveStatus(
                        elapsed = 18,
                        shortStatus = "2H",
                        longStatus = "Second Half",
                        score = FixtureLiveStatusModel.Score(home = 2, away = 1),
                    ),
            )
        val response =
            FixtureLiveStatusResponse(
                fixtureUid = "fixture-1",
                liveStatus =
                    FixtureLiveStatusResponse.LiveStatus(
                        elapsed = 18,
                        shortStatus = "2H",
                        longStatus = "Second Half",
                        score = FixtureLiveStatusResponse.Score(home = 2, away = 1),
                    ),
            )
        val document =
            FixtureResponseCacheDocument(
                snapshotJson = """{"fixtureUid":"fixture-1","liveStatus":{"elapsed":18,"shortStatus":"2H","longStatus":"Second Half","score":{"home":2,"away":1}}}""",
                etagHash = "etag-fresh",
            )

        every { matchDataQueryService.getFixtureLiveStatus("fixture-1") } returns DomainResult.Success(model)
        every { matchDataMapper.toFixtureLiveStatusResponse(model) } returns response
        every { cacheDocumentFactory.create(response) } returns document
        every { cacheManager.save("fixture-1", FixturePollingEndpoint.STATUS, document) } just Runs

        val result = service.getFixtureLiveStatus("fixture-1", """W/"etag-cached"""", bypassCacheRead = true)

        assertThat(result).isEqualTo(FixtureWebResult.Ok(document.snapshotJson, document.etagHash))
        verify(exactly = 0) { cacheManager.findEtagHash(any(), any()) }
        verify(exactly = 0) { cacheManager.findSnapshot(any(), any()) }
        verify(exactly = 0) { httpEtagHelper.matchesIfNoneMatch(any(), any()) }
        verify { matchDataQueryService.getFixtureLiveStatus("fixture-1") }
        verify { cacheDocumentFactory.create(response) }
        verify { cacheManager.save("fixture-1", FixturePollingEndpoint.STATUS, document) }
    }

    @Test
    fun `getFixtureLiveStatus - cache hit 이고 etag 가 다르면 cached snapshot 을 반환한다`() {
        every { cacheManager.findEtagHash("fixture-1", FixturePollingEndpoint.STATUS) } returns "etag-2"
        every { httpEtagHelper.matchesIfNoneMatch("""W/"etag-1"""", "etag-2") } returns false
        every { cacheManager.findSnapshot("fixture-1", FixturePollingEndpoint.STATUS) } returns
            FixtureWebCacheSnapshot(
                snapshotJson = """{"fixtureUid":"fixture-1"}""",
                etagHash = "etag-2",
            )

        val result = service.getFixtureLiveStatus("fixture-1", """W/"etag-1"""")

        assertThat(result).isEqualTo(
            FixtureWebResult.Ok(
                snapshotJson = """{"fixtureUid":"fixture-1"}""",
                etagHash = "etag-2",
            ),
        )
        verify(exactly = 0) { matchDataQueryService.getFixtureLiveStatus(any()) }
    }

    @Test
    fun `getFixtureLiveStatus - cache miss 이고 조회 실패면 Fail 을 반환한다`() {
        val error = DomainFail.NotFound(resource = "Fixture", id = "missing")

        every { cacheManager.findSnapshot("missing", FixturePollingEndpoint.STATUS) } returns null
        every { matchDataQueryService.getFixtureLiveStatus("missing") } returns DomainResult.Fail(error)

        val result = service.getFixtureLiveStatus("missing", null)

        assertThat(result).isEqualTo(FixtureWebResult.Fail(error))
        verify(exactly = 0) { cacheDocumentFactory.create(any<FixtureLiveStatusResponse>()) }
        verify(exactly = 0) { cacheManager.save(any(), any(), any()) }
    }
}
