package com.footballay.core.web.football.controller

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.web.football.cache.hash.FixtureHttpEtagHelper
import com.footballay.core.web.football.dto.FixtureInfoResponse
import com.footballay.core.web.football.service.FixtureWebResult
import com.footballay.core.web.football.service.FixtureWebService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class FixtureMatchControllerTest {
    private lateinit var webService: FixtureWebService
    private lateinit var httpEtagHelper: FixtureHttpEtagHelper
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        webService = mockk()
        httpEtagHelper = mockk()
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(FixtureMatchController(webService, httpEtagHelper))
                .build()
    }

    @Test
    fun `info endpoint returns name and shortName without korean-specific fields`() {
        every { webService.getFixtureInfo("fixture-1") } returns
            DomainResult.Success(
                FixtureInfoResponse(
                    fixtureUid = "fixture-1",
                    referee = null,
                    date = "2026-08-20 20:00",
                    league =
                        FixtureInfoResponse.LeagueInfo(
                            leagueUid = "league-1",
                            name = "Premier League",
                            shortName = "PL",
                            logo = null,
                        ),
                    home = null,
                    away = null,
                ),
            )

        mockMvc
            .get("/api/v1/football/fixtures/{uid}/info", "fixture-1")
            .andExpect {
                status { isOk() }
                jsonPath("$.league.name") { value("Premier League") }
                jsonPath("$.league.shortName") { value("PL") }
                jsonPath("$.league.koreanName") { doesNotExist() }
            }
    }

    @Test
    fun `status endpoint - Ok 결과를 application json body 와 etag 로 반환한다`() {
        every { webService.getFixtureLiveStatus("fixture-1", null, false) } returns
            FixtureWebResult.Ok(
                snapshotJson = """{"fixtureUid":"fixture-1","liveStatus":{"shortStatus":"NS","longStatus":"Not Started","elapsed":null,"score":{"home":0,"away":0}}}""",
                etagHash = "etag-1",
            )
        every { httpEtagHelper.toWeakEtag("etag-1") } returns """W/"etag-1""""

        mockMvc
            .get("/api/v1/football/fixtures/{uid}/status", "fixture-1")
            .andExpect {
                status { isOk() }
                header { string(HttpHeaders.ETAG, """W/"etag-1"""") }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json("""{"fixtureUid":"fixture-1","liveStatus":{"shortStatus":"NS","longStatus":"Not Started","elapsed":null,"score":{"home":0,"away":0}}}""")
                }
            }
    }

    @Test
    fun `status endpoint - NotModified 결과를 304 와 etag 로 반환한다`() {
        every { webService.getFixtureLiveStatus("fixture-1", """W/"etag-1"""", false) } returns FixtureWebResult.NotModified("etag-1")
        every { httpEtagHelper.toWeakEtag("etag-1") } returns """W/"etag-1""""

        mockMvc
            .get("/api/v1/football/fixtures/{uid}/status", "fixture-1") {
                header(HttpHeaders.IF_NONE_MATCH, """W/"etag-1"""")
            }.andExpect {
                status { isNotModified() }
                header { string(HttpHeaders.ETAG, """W/"etag-1"""") }
                content { string("") }
            }
    }

    @Test
    fun `status endpoint - X-Fixture-Cache-Control bypass 가 있으면 cache bypass 플래그를 전달한다`() {
        every { webService.getFixtureLiveStatus("fixture-1", null, true) } returns
            FixtureWebResult.Ok(
                snapshotJson = """{"fixtureUid":"fixture-1"}""",
                etagHash = "etag-bypass",
            )
        every { httpEtagHelper.toWeakEtag("etag-bypass") } returns """W/"etag-bypass""""

        mockMvc
            .get("/api/v1/football/fixtures/{uid}/status", "fixture-1") {
                header("X-Fixture-Cache-Control", "bypass")
            }.andExpect {
                status { isOk() }
                header { string(HttpHeaders.ETAG, """W/"etag-bypass"""") }
            }
    }

    @Test
    fun `status endpoint - Fail 결과를 domain fail 상태 코드로 반환한다`() {
        every { webService.getFixtureLiveStatus("missing", null, false) } returns
            FixtureWebResult.Fail(DomainFail.NotFound(resource = "Fixture", id = "missing"))

        mockMvc
            .get("/api/v1/football/fixtures/{uid}/status", "missing")
            .andExpect {
                status { isNotFound() }
            }
    }
}
