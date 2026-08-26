package com.footballay.core.web.football.localization

import com.footballay.core.domain.model.match.FixtureInfoModel
import com.footballay.core.domain.model.match.FixtureEventsModel
import com.footballay.core.domain.model.match.FixtureLineupModel
import com.footballay.core.domain.model.match.FixtureStatisticsModel
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.LeagueCoreLocalization
import com.footballay.core.infra.persistence.core.entity.PlayerCore
import com.footballay.core.infra.persistence.core.entity.PlayerCoreLocalization
import com.footballay.core.infra.persistence.core.entity.TeamCore
import com.footballay.core.infra.persistence.core.entity.TeamCoreLocalization
import com.footballay.core.infra.persistence.core.repository.LeagueCoreLocalizationRepository
import com.footballay.core.infra.persistence.core.repository.PlayerCoreLocalizationRepository
import com.footballay.core.infra.persistence.core.repository.TeamCoreLocalizationRepository
import com.footballay.core.localization.SupportedLocale
import io.mockk.every
import io.mockk.mockk
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
    fun `fixture info reflects requested league and team names with short names`() {
        val model =
            FixtureInfoModel(
                fixtureUid = "fixture-1",
                referee = null,
                date = "2026-08-20 20:00",
                league = FixtureInfoModel.LeagueInfo(name = "League", logo = null, leagueUid = "league-1"),
                home = FixtureInfoModel.TeamInfo(name = "Home", logo = null, teamUid = "team-1", playerColor = null),
                away = null,
            )
        every { leagueLocalizationRepository.findAllByCoreUidInAndLocaleIn(any(), any()) } returns
            listOf(
                LeagueCoreLocalization(leagueCore = LeagueCore(uid = "league-1", name = "League"), locale = SupportedLocale.KO, name = "리그", shortName = "리그 약칭"),
            )
        every { teamLocalizationRepository.findAllByCoreUidInAndLocaleIn(any(), any()) } returns
            listOf(
                TeamCoreLocalization(teamCore = TeamCore(uid = "team-1", name = "Home"), locale = SupportedLocale.KO, name = "홈 팀", shortName = "홈"),
            )

        val response = service.localizeFixtureInfo(model, SupportedLocale.KO)

        assertThat(response.league.name).isEqualTo("리그")
        assertThat(response.league.shortName).isEqualTo("리그 약칭")
        assertThat(response.home?.name).isEqualTo("홈 팀")
        assertThat(response.home?.shortName).isEqualTo("홈")
    }

    @Test
    fun `polling preparation reflects localized names in lineup events and statistics`() {
        val lineup = lineupWithOnePlayer()
        val events = eventsWithOnePlayer()
        val statistics = statisticsWithOnePlayer()
        every { teamLocalizationRepository.findAllByCoreUidInAndLocaleIn(any(), any()) } returns
            listOf(
                TeamCoreLocalization(teamCore = TeamCore(uid = "team-1", name = "Home"), locale = SupportedLocale.KO, name = "홈 팀", shortName = "홈"),
            )
        every { playerLocalizationRepository.findAllByCoreUidInAndLocaleIn(any(), any()) } returns
            listOf(
                PlayerCoreLocalization(playerCore = PlayerCore(uid = "player-1", name = "Player"), locale = SupportedLocale.KO, name = "선수", shortName = "선"),
            )

        val responses = service.preparePollingModels(lineup, events, statistics, SupportedLocale.entries)

        val home = responses[SupportedLocale.KO]?.lineup?.home
        assertThat(home?.teamName).isEqualTo("홈 팀")
        assertThat(home?.teamShortName).isEqualTo("홈")
        assertThat(home?.players?.single()?.name).isEqualTo("선수")
        assertThat(home?.players?.single()?.shortName).isEqualTo("선")
        assertThat(responses[SupportedLocale.KO]?.events?.events?.single()?.player?.shortName).isEqualTo("선")
        assertThat(responses[SupportedLocale.KO]?.statistics?.home?.playerStatistics?.single()?.player?.name).isEqualTo("선수")
    }

    private fun lineupWithOnePlayer(): FixtureLineupModel =
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

    private fun eventsWithOnePlayer(): FixtureEventsModel =
        FixtureEventsModel(
            fixtureUid = "fixture-1",
            events =
                listOf(
                    FixtureEventsModel.EventInfo(
                        sequence = 1,
                        elapsed = 10,
                        extraTime = null,
                        team = FixtureEventsModel.TeamInfo(name = "Home", teamUid = "team-1", playerColor = null),
                        player = FixtureEventsModel.PlayerInfo(name = "Player", number = null, matchPlayerUid = "match-player-1", playerUid = "player-1"),
                        assist = null,
                        type = "Goal",
                        detail = "Normal Goal",
                        comments = null,
                    ),
                ),
        )

    private fun statisticsWithOnePlayer(): FixtureStatisticsModel =
        FixtureStatisticsModel(
            fixture = FixtureStatisticsModel.FixtureBasic("fixture-1", null, "1H"),
            home =
                FixtureStatisticsModel.TeamWithStatistics(
                    team = FixtureStatisticsModel.TeamInfo(name = "Home", logo = null, teamUid = "team-1", playerColor = null),
                    teamStatistics = mockk(),
                    playerStatistics =
                        listOf(
                            FixtureStatisticsModel.PlayerWithStatistics(
                                player = FixtureStatisticsModel.PlayerInfoBasic(name = "Player", photo = null, position = null, number = null, matchPlayerUid = "match-player-1", playerUid = "player-1"),
                                statistics = mockk(),
                            ),
                        ),
                ),
            away = null,
        )
}
