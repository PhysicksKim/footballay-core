package com.footballay.core.web.football.localization

import com.footballay.core.domain.model.match.FixtureInfoModel
import com.footballay.core.domain.model.match.FixtureLineupModel
import com.footballay.core.infra.persistence.core.repository.LeagueCoreLocalizationRepository
import com.footballay.core.infra.persistence.core.repository.PlayerCoreLocalizationRepository
import com.footballay.core.infra.persistence.core.repository.TeamCoreLocalizationRepository
import com.footballay.core.localization.SupportedLocale
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FootballResponseLocalizationServiceTest {
    private lateinit var leagueLocalizationRepository: LeagueCoreLocalizationRepository
    private lateinit var teamLocalizationRepository: TeamCoreLocalizationRepository
    private lateinit var playerLocalizationRepository: PlayerCoreLocalizationRepository
    private lateinit var service: FootballResponseLocalizationService

    @BeforeEach
    fun setUp() {
        leagueLocalizationRepository = mockk()
        teamLocalizationRepository = mockk()
        playerLocalizationRepository = mockk()
        service =
            FootballResponseLocalizationService(
                leagueLocalizationRepository,
                teamLocalizationRepository,
                playerLocalizationRepository,
            )
    }

    @Test
    fun `fixture info reads only league and team localization once`() {
        val model =
            FixtureInfoModel(
                fixtureUid = "fixture-1",
                referee = null,
                date = "2026-08-20 20:00",
                league = FixtureInfoModel.LeagueInfo(name = "League", logo = null, leagueUid = "league-1"),
                home = FixtureInfoModel.TeamInfo(name = "Home", logo = null, teamUid = "team-1", playerColor = null),
                away = null,
            )
        every { leagueLocalizationRepository.findAllByCoreUidInAndLocaleIn(any(), any()) } returns emptyList()
        every { teamLocalizationRepository.findAllByCoreUidInAndLocaleIn(any(), any()) } returns emptyList()

        val response = service.localizeFixtureInfo(model, SupportedLocale.KO)

        assertThat(response.league.name).isEqualTo("League")
        assertThat(response.home?.name).isEqualTo("Home")
        val requestedLocales = setOf(SupportedLocale.KO, SupportedLocale.EN)

        verify(exactly = 1) {
            leagueLocalizationRepository.findAllByCoreUidInAndLocaleIn(
                setOf("league-1"),
                requestedLocales,
            )
        }
        verify(exactly = 1) {
            teamLocalizationRepository.findAllByCoreUidInAndLocaleIn(
                setOf("team-1"),
                requestedLocales,
            )
        }
        verify(exactly = 0) { playerLocalizationRepository.findAllByCoreUidInAndLocaleIn(any(), any()) }
    }

    @Test
    fun `polling preparation reads team and player localization once for all locales`() {
        val lineup =
            FixtureLineupModel(
                fixtureUid = "fixture-1",
                lineup =
                    FixtureLineupModel.Lineup(
                        home =
                            FixtureLineupModel.StartLineup(
                                teamName = "Home",
                                teamUid = "team-1",
                                formation = null,
                                players =
                                    listOf(
                                        FixtureLineupModel.LineupPlayer(
                                            name = "Player",
                                            number = null,
                                            photo = null,
                                            position = null,
                                            grid = null,
                                            substitute = false,
                                            matchPlayerUid = "match-player-1",
                                            playerUid = "player-1",
                                        ),
                                    ),
                                substitutes = emptyList(),
                                playerColor = null,
                            ),
                        away = null,
                    ),
            )
        every { teamLocalizationRepository.findAllByCoreUidInAndLocaleIn(any(), any()) } returns emptyList()
        every { playerLocalizationRepository.findAllByCoreUidInAndLocaleIn(any(), any()) } returns emptyList()

        val responses = service.preparePollingModels(lineup, null, null, SupportedLocale.entries)

        assertThat(responses[SupportedLocale.EN]?.lineup?.home?.teamName).isEqualTo("Home")
        assertThat(responses[SupportedLocale.EN]?.lineup?.home?.players?.single()?.name).isEqualTo("Player")
        val supportedLocales = SupportedLocale.entries.toSet()

        verify(exactly = 1) {
            teamLocalizationRepository.findAllByCoreUidInAndLocaleIn(
                setOf("team-1"),
                supportedLocales,
            )
        }
        verify(exactly = 1) {
            playerLocalizationRepository.findAllByCoreUidInAndLocaleIn(
                setOf("player-1"),
                supportedLocales,
            )
        }
        verify(exactly = 0) { leagueLocalizationRepository.findAllByCoreUidInAndLocaleIn(any(), any()) }
    }
}
