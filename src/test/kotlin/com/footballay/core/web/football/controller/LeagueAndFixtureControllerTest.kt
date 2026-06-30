package com.footballay.core.web.football.controller

import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.facade.MockDataReadOption
import com.footballay.core.web.football.dto.AvailableLeagueResponse
import com.footballay.core.web.football.dto.FixtureByLeagueResponse
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
                        nameKo = null,
                        logo = null,
                    ),
                ),
            )

        mockMvc
            .get("/api/v1/football/leagues/available")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].uid") { value("api-league") }
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
                        nameKo = null,
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
                        nameKo = null,
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
            .get("/api/v1/football/leagues/{leagueUid}/fixtures", "league-1") {
                param("date", "2026-06-01")
                param("mode", "exact")
                param("timezone", "UTC")
                header(MockDataReadOptionResolver.HEADER_NAME, "include")
            }.andExpect {
                status { isOk() }
                jsonPath("$[0].uid") { value("mock-fixture") }
            }
    }
}
