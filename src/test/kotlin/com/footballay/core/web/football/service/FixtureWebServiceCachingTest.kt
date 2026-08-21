package com.footballay.core.web.football.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.model.match.FixtureLiveStatusModel
import com.footballay.core.domain.model.match.FixtureEventsModel
import com.footballay.core.infra.query.MatchDataQueryService
import com.footballay.core.web.football.cache.FixturePollingEndpoint
import com.footballay.core.web.football.cache.FixtureWebCacheIdentity
import com.footballay.core.web.football.cache.FixtureWebCacheManager
import com.footballay.core.web.football.cache.FixtureWebCacheSnapshot
import com.footballay.core.web.football.cache.hash.FixtureHttpEtagHelper
import com.footballay.core.web.football.cache.hash.FixtureResponseCacheDocument
import com.footballay.core.web.football.cache.hash.FixtureResponseCacheDocumentFactory
import com.footballay.core.web.football.dto.FixtureLiveStatusResponse
import com.footballay.core.web.football.dto.FixtureEventsResponse
import com.footballay.core.web.football.localization.LocalizedFixtureEventsModel
import com.footballay.core.web.football.mapper.MatchDataMapper
import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.football.localization.FootballResponseLocalizationService
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
    private lateinit var localizationService: FootballResponseLocalizationService
    private lateinit var cacheManager: FixtureWebCacheManager
    private lateinit var cacheDocumentFactory: FixtureResponseCacheDocumentFactory
    private lateinit var httpEtagHelper: FixtureHttpEtagHelper
    private lateinit var service: FixtureWebService

    @BeforeEach
    fun setUp() {
        matchDataQueryService = mockk()
        matchDataMapper = mockk()
        localizationService = mockk()
        cacheManager = mockk()
        cacheDocumentFactory = mockk()
        httpEtagHelper = mockk()
        service =
            FixtureWebService(
                matchDataQueryService = matchDataQueryService,
                matchDataMapper = matchDataMapper,
                localizationService = localizationService,
                cacheManager = cacheManager,
                cacheDocumentFactory = cacheDocumentFactory,
                httpEtagHelper = httpEtagHelper,
            )
    }

    @Test
    fun `getFixtureLiveStatus - cache hit 이고 etag 가 같으면 NotModified 를 반환한다`() {
        val identity = FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.STATUS, null)
        every { cacheManager.findEtagHash(identity) } returns "etag-1"
        every { httpEtagHelper.matchesIfNoneMatch("""W/"etag-1"""", "etag-1") } returns true

        val result = service.getFixtureLiveStatus("fixture-1", """W/"etag-1"""")

        assertThat(result).isEqualTo(FixtureWebResult.NotModified("etag-1"))
        verify(exactly = 0) { cacheManager.findSnapshot(any()) }
        verify(exactly = 0) { matchDataQueryService.getFixtureLiveStatus(any()) }
    }

    @Test
    fun `getFixtureLiveStatus - cache miss 면 조회 후 저장하고 Ok 를 반환한다`() {
        val identity = FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.STATUS, null)
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

        every { cacheManager.findSnapshot(identity) } returns null
        every { matchDataQueryService.getFixtureLiveStatus("fixture-1") } returns DomainResult.Success(model)
        every { matchDataMapper.toFixtureLiveStatusResponse(model) } returns response
        every { cacheDocumentFactory.create(response) } returns document
        every { cacheManager.save(identity, document) } just Runs

        val result = service.getFixtureLiveStatus("fixture-1", null)

        assertThat(result).isEqualTo(FixtureWebResult.Ok(document.snapshotJson, document.etagHash))
        verify { cacheManager.save(identity, document) }
    }

    @Test
    fun `getFixtureLiveStatus - bypass cache read 이면 캐시가 있어도 원본 조회를 강제한다`() {
        val identity = FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.STATUS, null)
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
        every { cacheManager.save(identity, document) } just Runs

        val result = service.getFixtureLiveStatus("fixture-1", """W/"etag-cached"""", bypassCacheRead = true)

        assertThat(result).isEqualTo(FixtureWebResult.Ok(document.snapshotJson, document.etagHash))
        verify(exactly = 0) { cacheManager.findEtagHash(any()) }
        verify(exactly = 0) { cacheManager.findSnapshot(any()) }
        verify(exactly = 0) { httpEtagHelper.matchesIfNoneMatch(any(), any()) }
        verify { matchDataQueryService.getFixtureLiveStatus("fixture-1") }
        verify { cacheDocumentFactory.create(response) }
        verify { cacheManager.save(identity, document) }
    }

    @Test
    fun `getFixtureLiveStatus - cache hit 이고 etag 가 다르면 cached snapshot 을 반환한다`() {
        val identity = FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.STATUS, null)
        every { cacheManager.findEtagHash(identity) } returns "etag-2"
        every { httpEtagHelper.matchesIfNoneMatch("""W/"etag-1"""", "etag-2") } returns false
        every { cacheManager.findSnapshot(identity) } returns
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
    fun `getFixtureEvents - cache hit 이면 localization 조회를 하지 않는다`() {
        val identity = FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.EVENTS, SupportedLocale.KO)
        every { cacheManager.findSnapshot(identity) } returns
            FixtureWebCacheSnapshot(snapshotJson = "{\"fixtureUid\":\"fixture-1\"}", etagHash = "etag-1")

        val result = service.getFixtureEvents("fixture-1", null, locale = SupportedLocale.KO)

        assertThat(result).isEqualTo(FixtureWebResult.Ok("{\"fixtureUid\":\"fixture-1\"}", "etag-1"))
        verify(exactly = 0) { matchDataQueryService.getFixtureEvents(any()) }
        verify(exactly = 0) { localizationService.localizeEvents(any(), any()) }
    }

    @Test
    fun `getFixtureEvents - 다른 locale etag 는 304 를 만들지 않는다`() {
        val enIdentity = FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.EVENTS, SupportedLocale.EN)
        val koIdentity = FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.EVENTS, SupportedLocale.KO)
        every { cacheManager.findEtagHash(enIdentity) } returns "etag-en"
        every { httpEtagHelper.matchesIfNoneMatch("""W/"etag-ko""", "etag-en") } returns false
        every { cacheManager.findSnapshot(enIdentity) } returns FixtureWebCacheSnapshot("{\"fixtureUid\":\"fixture-1\"}", "etag-en")

        val result = service.getFixtureEvents("fixture-1", """W/"etag-ko""", locale = SupportedLocale.EN)

        assertThat(result).isEqualTo(FixtureWebResult.Ok("{\"fixtureUid\":\"fixture-1\"}", "etag-en"))
        verify(exactly = 0) { cacheManager.findEtagHash(koIdentity) }
        verify(exactly = 0) { cacheManager.findSnapshot(koIdentity) }
    }

    @Test
    fun `getFixtureEvents - 같은 locale etag 가 같으면 NotModified 를 반환한다`() {
        val identity = FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.EVENTS, SupportedLocale.KO)
        every { cacheManager.findEtagHash(identity) } returns "etag-ko"
        every { httpEtagHelper.matchesIfNoneMatch("""W/"etag-ko""", "etag-ko") } returns true

        val result = service.getFixtureEvents("fixture-1", """W/"etag-ko""", locale = SupportedLocale.KO)

        assertThat(result).isEqualTo(FixtureWebResult.NotModified("etag-ko"))
        verify(exactly = 0) { cacheManager.findSnapshot(any()) }
        verify(exactly = 0) { matchDataQueryService.getFixtureEvents(any()) }
    }

    @Test
    fun `getFixtureEvents - cache miss 이면 localized response 를 저장한다`() {
        val identity = FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.EVENTS, SupportedLocale.EN)
        val model = mockk<FixtureEventsModel>()
        val localizedModel = mockk<LocalizedFixtureEventsModel>()
        val response = FixtureEventsResponse("fixture-1", emptyList())
        val document = FixtureResponseCacheDocument("{\"fixtureUid\":\"fixture-1\",\"events\":[]}", "etag-en")
        every { cacheManager.findSnapshot(identity) } returns null
        every { matchDataQueryService.getFixtureEvents("fixture-1") } returns DomainResult.Success(model)
        every { localizationService.localizeEvents(model, SupportedLocale.EN) } returns localizedModel
        every { matchDataMapper.toFixtureEventsResponse(localizedModel) } returns response
        every { cacheDocumentFactory.create(response) } returns document
        every { cacheManager.save(identity, document) } just Runs

        val result = service.getFixtureEvents("fixture-1", null, locale = SupportedLocale.EN)

        assertThat(result).isEqualTo(FixtureWebResult.Ok(document.snapshotJson, document.etagHash))
        verify { cacheManager.save(identity, document) }
    }

    @Test
    fun `getFixtureEvents - bypass 는 cache read 없이 localized response 를 저장한다`() {
        val identity = FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.EVENTS, SupportedLocale.KO)
        val model = mockk<FixtureEventsModel>()
        val localizedModel = mockk<LocalizedFixtureEventsModel>()
        val response = FixtureEventsResponse("fixture-1", emptyList())
        val document = FixtureResponseCacheDocument("{\"fixtureUid\":\"fixture-1\",\"events\":[]}", "etag-ko")
        every { matchDataQueryService.getFixtureEvents("fixture-1") } returns DomainResult.Success(model)
        every { localizationService.localizeEvents(model, SupportedLocale.KO) } returns localizedModel
        every { matchDataMapper.toFixtureEventsResponse(localizedModel) } returns response
        every { cacheDocumentFactory.create(response) } returns document
        every { cacheManager.save(identity, document) } just Runs

        val result = service.getFixtureEvents("fixture-1", """W/"etag-old""", bypassCacheRead = true, locale = SupportedLocale.KO)

        assertThat(result).isEqualTo(FixtureWebResult.Ok(document.snapshotJson, document.etagHash))
        verify(exactly = 0) { cacheManager.findEtagHash(any()) }
        verify(exactly = 0) { cacheManager.findSnapshot(any()) }
        verify { cacheManager.save(identity, document) }
    }

    @Test
    fun `getFixtureLiveStatus - cache miss 이고 조회 실패면 Fail 을 반환한다`() {
        val identity = FixtureWebCacheIdentity("missing", FixturePollingEndpoint.STATUS, null)
        val error = DomainFail.NotFound(resource = "Fixture", id = "missing")

        every { cacheManager.findSnapshot(identity) } returns null
        every { matchDataQueryService.getFixtureLiveStatus("missing") } returns DomainResult.Fail(error)

        val result = service.getFixtureLiveStatus("missing", null)

        assertThat(result).isEqualTo(FixtureWebResult.Fail(error))
        verify(exactly = 0) { cacheDocumentFactory.create(any<FixtureLiveStatusResponse>()) }
        verify(exactly = 0) { cacheManager.save(any(), any()) }
    }
}
