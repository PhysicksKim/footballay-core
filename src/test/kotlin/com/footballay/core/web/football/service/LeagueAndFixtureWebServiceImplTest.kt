package com.footballay.core.web.football.service

import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.facade.DesktopFixtureFacade
import com.footballay.core.domain.facade.DesktopLeagueFacade
import com.footballay.core.domain.facade.MockDataReadOption
import com.footballay.core.domain.model.FixtureModel
import com.footballay.core.domain.model.LeagueModel
import com.footballay.core.web.football.dto.AvailableLeagueResponse
import com.footballay.core.web.football.dto.FixtureByLeagueResponse
import com.footballay.core.web.football.localization.FootballResponseLocalizationService
import com.footballay.core.web.football.localization.LocalizedAvailableLeagueModel
import com.footballay.core.web.football.localization.LocalizedFixtureByLeagueModel
import com.footballay.core.web.football.mapper.MatchDataMapper
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
    private lateinit var localizationService: FootballResponseLocalizationService
    private lateinit var matchDataMapper: MatchDataMapper
    private lateinit var webService: LeagueAndFixtureWebServiceImpl

    @BeforeEach
    fun setUp() {
        desktopLeagueFacade = mockk()
        desktopFixtureFacade = mockk()
        localizationService = mockk()
        matchDataMapper = mockk()
        webService =
            LeagueAndFixtureWebServiceImpl(
                desktopLeagueFacade,
                desktopFixtureFacade,
                localizationService,
                matchDataMapper,
            )
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
                        photo = null,
                        available = true,
                    ),
                ),
            )

        every { localizationService.localizeAvailableLeagues(any(), any()) } returns
            listOf(LocalizedAvailableLeagueModel("mock-league", "Mock League", null, null))
        every { matchDataMapper.toAvailableLeagueResponses(any()) } returns
            listOf(AvailableLeagueResponse(uid = "mock-league", name = "Mock League", shortName = null, logo = null))

        val result = webService.getAvailableLeagues(option)

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        assertThat((result as DomainResult.Success).value.map { it.uid }).containsExactly("mock-league")
        verify(exactly = 1) { desktopLeagueFacade.getAvailableLeagues(option) }
        verify(exactly = 1) { localizationService.localizeAvailableLeagues(any(), any()) }
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

        every { localizationService.localizeFixturesByLeague(any(), any()) } returns
            listOf(
                LocalizedFixtureByLeagueModel(
                    uid = "mock-fixture",
                    kickoff = Instant.parse("2026-06-01T10:00:00Z"),
                    round = "Mock Round",
                    homeTeam = null,
                    awayTeam = null,
                    status = FixtureModel.Status("Not Started", FixtureModel.StatusCode.NS, null, null),
                    score = FixtureModel.Score(null, null),
                    available = true,
                ),
            )
        every { matchDataMapper.toFixtureByLeagueResponses(any()) } returns
            listOf(
                FixtureByLeagueResponse(
                    uid = "mock-fixture",
                    kickoff = "2026-06-01T10:00:00Z",
                    round = "Mock Round",
                    homeTeam = null,
                    awayTeam = null,
                    status = FixtureByLeagueResponse.StatusInfo("Not Started", "NS", null),
                    score = FixtureByLeagueResponse.ScoreInfo(null, null),
                    available = true,
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
        verify(exactly = 1) { localizationService.localizeFixturesByLeague(any(), any()) }
    }
}
