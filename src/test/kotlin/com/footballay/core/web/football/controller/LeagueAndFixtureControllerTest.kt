package com.footballay.core.web.football.controller

import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.facade.MockDataReadOption
import com.footballay.core.web.football.dto.AvailableLeagueResponse
import com.footballay.core.web.football.dto.FixtureByLeagueResponse
import com.footballay.core.web.football.dto.FixtureDatesByLeagueResponse
import com.footballay.core.web.football.service.LeagueAndFixtureWebService
import com.footballay.core.web.football.service.MockDataReadOptionResolver
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Instant
import java.time.ZoneId

class LeagueAndFixtureControllerTest {
    private lateinit var webService: LeagueAndFixtureWebService
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        webService = mockk()
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(LeagueAndFixtureController(webService))
                .build()
    }

    @Test
    fun `available leagues - 헤더가 없으면 기본 mock data read option을 전달한다`() {
        every { webService.getAvailableLeagues(MockDataReadOption.DEFAULT) } returns
            DomainResult.Success(
                listOf(
                    AvailableLeagueResponse(
                        uid = "api-league",
                        name = "ApiSports League",
                        shortName = "APL",
                        logo = null,
                    ),
                ),
            )

        mockMvc
            .get("/api/v1/football/leagues/available")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].uid") { value("api-league") }
                jsonPath("$[0].shortName") { value("APL") }
                jsonPath("$[0].nameKo") { doesNotExist() }
            }
    }

    @Test
    fun `available leagues - include 헤더가 있으면 mock data 포함 option을 전달한다`() {
        every { webService.getAvailableLeagues(MockDataReadOption(includeMockData = true)) } returns
            DomainResult.Success(
                listOf(
                    AvailableLeagueResponse(
                        uid = "mock-league",
                        name = "Mock League",
                        shortName = null,
                        logo = null,
                    ),
                ),
            )

        mockMvc
            .get("/api/v1/football/leagues/available") {
                header(MockDataReadOptionResolver.HEADER_NAME, "include")
            }.andExpect {
                status { isOk() }
                jsonPath("$[0].uid") { value("mock-league") }
            }
    }

    @Test
    fun `available leagues - 알 수 없는 헤더 값은 기본 mock data read option을 전달한다`() {
        every { webService.getAvailableLeagues(MockDataReadOption.DEFAULT) } returns
            DomainResult.Success(
                listOf(
                    AvailableLeagueResponse(
                        uid = "api-league",
                        name = "ApiSports League",
                        shortName = null,
                        logo = null,
                    ),
                ),
            )

        mockMvc
            .get("/api/v1/football/leagues/available") {
                header(MockDataReadOptionResolver.HEADER_NAME, "enabled")
            }.andExpect {
                status { isOk() }
                jsonPath("$[0].uid") { value("api-league") }
            }
    }

    @Test
    fun `fixtures by league - include 헤더가 있으면 mock data 포함 option을 전달한다`() {
        every {
            webService.getFixturesByLeague(
                leagueUid = "league-1",
                at = Instant.parse("2026-06-01T00:00:00Z"),
                mode = "exact",
                zoneId = ZoneId.of("UTC"),
                option = MockDataReadOption(includeMockData = true),
            )
        } returns
            DomainResult.Success(
                listOf(
                    FixtureByLeagueResponse(
                        uid = "mock-fixture",
                        kickoff = "2026-06-01T10:00:00Z",
                        round = "Mock Round",
                        homeTeam = null,
                        awayTeam = null,
                        status =
                            FixtureByLeagueResponse.StatusInfo(
                                longStatus = "Not Started",
                                shortStatus = "NS",
                                elapsed = null,
                            ),
                        score =
                            FixtureByLeagueResponse.ScoreInfo(
                                home = null,
                                away = null,
                            ),
                        available = true,
                    ),
                ),
            )

        mockMvc
            .get("/api/v1/football/leagues/{leagueUid}/fixtures", " league-1 ") {
                param("date", " 2026-06-01 ")
                param("mode", "exact")
                param("timezone", " UTC ")
                header(MockDataReadOptionResolver.HEADER_NAME, "include")
            }.andExpect {
                status { isOk() }
                jsonPath("$[0].uid") { value("mock-fixture") }
            }
    }

    @Test
    fun `fixture dates by league - inclusive 범위와 timezone을 전달한다`() {
        every {
            webService.getFixtureDatesByLeague(
                leagueUid = "league-1",
                startInclusive = Instant.parse("2026-08-01T00:00:00Z"),
                endExclusive = Instant.parse("2026-09-01T00:00:00Z"),
                zoneId = ZoneId.of("UTC"),
                option = MockDataReadOption.DEFAULT,
            )
        } returns DomainResult.Success(FixtureDatesByLeagueResponse(listOf("2026-08-01", "2026-08-31")))

        mockMvc
            .get("/api/v1/football/leagues/{leagueUid}/fixtures/dates", "league-1") {
                param("startDate", "2026-08-01")
                param("endDate", "2026-08-31")
            }.andExpect {
                status { isOk() }
                jsonPath("$.dates[0]") { value("2026-08-01") }
                jsonPath("$.dates[1]") { value("2026-08-31") }
            }
    }

    @Test
    fun `fixture dates by league - timezone 또는 날짜 범위가 잘못되면 bad request를 반환한다`() {
        mockMvc
            .get("/api/v1/football/leagues/{leagueUid}/fixtures/dates", "   ") {
                param("startDate", "2026-08-01")
                param("endDate", "2026-08-31")
            }.andExpect {
                status { isBadRequest() }
            }

        mockMvc
            .get("/api/v1/football/leagues/{leagueUid}/fixtures/dates", "league-1") {
                param("startDate", "2026-08-02")
                param("endDate", "2026-08-01")
            }.andExpect {
                status { isBadRequest() }
            }

        mockMvc
            .get("/api/v1/football/leagues/{leagueUid}/fixtures/dates", "league-1") {
                param("startDate", "2026-08-01")
                param("endDate", "2026-08-31")
                param("timezone", "invalid/timezone")
            }.andExpect {
                status { isBadRequest() }
            }

        mockMvc
            .get("/api/v1/football/leagues/{leagueUid}/fixtures/dates", "league-1") {
                param("startDate", "2026-08-xx")
                param("endDate", "2026-08-31")
            }.andExpect {
                status { isBadRequest() }
            }
    }
}
