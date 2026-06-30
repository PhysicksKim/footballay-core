package com.footballay.core.web.football.service

import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.facade.DesktopFixtureFacade
import com.footballay.core.domain.facade.DesktopLeagueFacade
import com.footballay.core.domain.facade.MockDataReadOption
import com.footballay.core.domain.model.FixtureModel
import com.footballay.core.domain.model.LeagueModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneOffset

class LeagueAndFixtureWebServiceImplTest {
    private lateinit var desktopLeagueFacade: DesktopLeagueFacade
    private lateinit var desktopFixtureFacade: DesktopFixtureFacade
    private lateinit var webService: LeagueAndFixtureWebServiceImpl

    @BeforeEach
    fun setUp() {
        desktopLeagueFacade = mockk()
        desktopFixtureFacade = mockk()
        webService = LeagueAndFixtureWebServiceImpl(desktopLeagueFacade, desktopFixtureFacade)
    }

    @Test
    fun `getAvailableLeagues는 mock data read option을 league facade로 전달한다`() {
        val option = MockDataReadOption(includeMockData = true)
        every { desktopLeagueFacade.getAvailableLeagues(option) } returns
            DomainResult.Success(
                listOf(
                    LeagueModel(
                        uid = "mock-league",
                        name = "Mock League",
                        nameKo = null,
                        photo = null,
                        available = true,
                    ),
                ),
            )

        val result = webService.getAvailableLeagues(option)

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        assertThat((result as DomainResult.Success).value.map { it.uid }).containsExactly("mock-league")
        verify(exactly = 1) { desktopLeagueFacade.getAvailableLeagues(option) }
    }

    @Test
    fun `getFixturesByLeague는 mock data read option을 fixture facade로 전달한다`() {
        val option = MockDataReadOption(includeMockData = true)
        val at = Instant.parse("2026-06-01T00:00:00Z")
        every {
            desktopFixtureFacade.getFixturesByLeague(
                leagueUid = "league-1",
                at = at,
                mode = "exact",
                zoneId = ZoneOffset.UTC,
                option = option,
            )
        } returns
            DomainResult.Success(
                listOf(
                    FixtureModel(
                        uid = "mock-fixture",
                        leagueUid = "league-1",
                        schedule =
                            FixtureModel.FixtureSchedule(
                                kickoffAt = Instant.parse("2026-06-01T10:00:00Z"),
                                round = "Mock Round",
                            ),
                        homeTeam = null,
                        awayTeam = null,
                        status =
                            FixtureModel.Status(
                                statusText = "Not Started",
                                code = FixtureModel.StatusCode.NS,
                                elapsed = null,
                                extra = null,
                            ),
                        score =
                            FixtureModel.Score(
                                home = null,
                                away = null,
                            ),
                        available = true,
                    ),
                ),
            )

        val result =
            webService.getFixturesByLeague(
                leagueUid = "league-1",
                at = at,
                mode = "exact",
                zoneId = ZoneOffset.UTC,
                option = option,
            )

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        assertThat((result as DomainResult.Success).value.map { it.uid }).containsExactly("mock-fixture")
        verify(exactly = 1) {
            desktopFixtureFacade.getFixturesByLeague(
                leagueUid = "league-1",
                at = at,
                mode = "exact",
                zoneId = ZoneOffset.UTC,
                option = option,
            )
        }
    }
}
