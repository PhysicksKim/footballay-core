package com.footballay.core.web.football.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.cache.matchdata.polling.FixturePollingEndpoint
import com.footballay.core.cache.matchdata.polling.FixtureWebCacheSnapshot
import com.footballay.core.cache.matchdata.polling.MatchDataPollingCacheManager
import com.footballay.core.cache.matchdata.polling.hash.FixtureHttpEtagHelper
import com.footballay.core.cache.matchdata.polling.hash.FixtureResponseCacheDocument
import com.footballay.core.matchdata.facade.MatchDataFacade
import com.footballay.core.web.football.mapper.MatchDataMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FixtureWebServiceCachingTest {
    private lateinit var matchDataFacade: MatchDataFacade
    private lateinit var matchDataMapper: MatchDataMapper
    private lateinit var pollingCacheManager: MatchDataPollingCacheManager
    private lateinit var httpEtagHelper: FixtureHttpEtagHelper
    private lateinit var service: FixtureWebService

    @BeforeEach
    fun setUp() {
        matchDataFacade = mockk()
        matchDataMapper = mockk()
        pollingCacheManager = mockk()
        httpEtagHelper = mockk()
        service =
            FixtureWebService(
                matchDataFacade = matchDataFacade,
                matchDataMapper = matchDataMapper,
                pollingCacheManager = pollingCacheManager,
                httpEtagHelper = httpEtagHelper,
            )
    }

    @Test
    fun `getFixtureLiveStatus - cache hit 이고 etag 가 같으면 NotModified 를 반환한다`() {
        every { pollingCacheManager.findEtagHash("fixture-1", FixturePollingEndpoint.STATUS) } returns "etag-1"
        every { httpEtagHelper.matchesIfNoneMatch("""W/"etag-1"""", "etag-1") } returns true

        val result = service.getFixtureLiveStatus("fixture-1", """W/"etag-1"""")

        assertThat(result).isEqualTo(FixtureWebResult.NotModified("etag-1"))
        verify(exactly = 0) { pollingCacheManager.findSnapshot(any(), any()) }
        verify(exactly = 0) { pollingCacheManager.refreshEndpoint(any(), any()) }
    }

    @Test
    fun `getFixtureLiveStatus - cache miss 면 조회 후 저장하고 Ok 를 반환한다`() {
        val document =
            FixtureResponseCacheDocument(
                snapshotJson = """{"fixtureUid":"fixture-1","liveStatus":{"elapsed":17,"shortStatus":"1H","longStatus":"First Half","score":{"home":1,"away":0}}}""",
                etagHash = "etag-2",
            )

        every { pollingCacheManager.findSnapshot("fixture-1", FixturePollingEndpoint.STATUS) } returns null
        every { pollingCacheManager.refreshEndpoint("fixture-1", FixturePollingEndpoint.STATUS) } returns DomainResult.Success(document)

        val result = service.getFixtureLiveStatus("fixture-1", null)

        assertThat(result).isEqualTo(FixtureWebResult.Ok(document.snapshotJson, document.etagHash))
        verify { pollingCacheManager.refreshEndpoint("fixture-1", FixturePollingEndpoint.STATUS) }
    }

    @Test
    fun `getFixtureLiveStatus - bypass cache read 이면 캐시가 있어도 원본 조회를 강제한다`() {
        val document =
            FixtureResponseCacheDocument(
                snapshotJson = """{"fixtureUid":"fixture-1","liveStatus":{"elapsed":18,"shortStatus":"2H","longStatus":"Second Half","score":{"home":2,"away":1}}}""",
                etagHash = "etag-fresh",
            )

        every { pollingCacheManager.refreshEndpoint("fixture-1", FixturePollingEndpoint.STATUS) } returns DomainResult.Success(document)

        val result = service.getFixtureLiveStatus("fixture-1", """W/"etag-cached"""", bypassCacheRead = true)

        assertThat(result).isEqualTo(FixtureWebResult.Ok(document.snapshotJson, document.etagHash))
        verify(exactly = 0) { pollingCacheManager.findEtagHash(any(), any()) }
        verify(exactly = 0) { pollingCacheManager.findSnapshot(any(), any()) }
        verify(exactly = 0) { httpEtagHelper.matchesIfNoneMatch(any(), any()) }
        verify { pollingCacheManager.refreshEndpoint("fixture-1", FixturePollingEndpoint.STATUS) }
    }

    @Test
    fun `getFixtureLiveStatus - cache hit 이고 etag 가 다르면 cached snapshot 을 반환한다`() {
        every { pollingCacheManager.findEtagHash("fixture-1", FixturePollingEndpoint.STATUS) } returns "etag-2"
        every { httpEtagHelper.matchesIfNoneMatch("""W/"etag-1"""", "etag-2") } returns false
        every { pollingCacheManager.findSnapshot("fixture-1", FixturePollingEndpoint.STATUS) } returns
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
        verify(exactly = 0) { pollingCacheManager.refreshEndpoint(any(), any()) }
    }

    @Test
    fun `getFixtureLiveStatus - cache miss 이고 조회 실패면 Fail 을 반환한다`() {
        val error = DomainFail.NotFound(resource = "Fixture", id = "missing")

        every { pollingCacheManager.findSnapshot("missing", FixturePollingEndpoint.STATUS) } returns null
        every { pollingCacheManager.refreshEndpoint("missing", FixturePollingEndpoint.STATUS) } returns DomainResult.Fail(error)

        val result = service.getFixtureLiveStatus("missing", null)

        assertThat(result).isEqualTo(FixtureWebResult.Fail(error))
        verify { pollingCacheManager.refreshEndpoint("missing", FixturePollingEndpoint.STATUS) }
    }
}
