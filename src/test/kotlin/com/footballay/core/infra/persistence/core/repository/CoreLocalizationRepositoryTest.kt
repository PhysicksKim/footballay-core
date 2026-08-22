package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.LeagueCoreLocalization
import com.footballay.core.infra.persistence.core.entity.PlayerCore
import com.footballay.core.infra.persistence.core.entity.PlayerCoreLocalization
import com.footballay.core.infra.persistence.core.entity.TeamCore
import com.footballay.core.infra.persistence.core.entity.TeamCoreLocalization
import com.footballay.core.localization.SupportedLocale
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

/** Core localization의 JPA mapping과 locale 변환을 검증합니다. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CoreLocalizationRepositoryTest {
    @Autowired
    private lateinit var playerCoreRepository: PlayerCoreRepository

    @Autowired
    private lateinit var teamCoreRepository: TeamCoreRepository

    @Autowired
    private lateinit var leagueCoreRepository: LeagueCoreRepository

    @Autowired
    private lateinit var playerLocalizationRepository: PlayerCoreLocalizationRepository

    @Autowired
    private lateinit var teamLocalizationRepository: TeamCoreLocalizationRepository

    @Autowired
    private lateinit var leagueLocalizationRepository: LeagueCoreLocalizationRepository

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Test
    fun `localizations store lowercase locale and allow null or blank names`() {
        val player = playerCoreRepository.save(PlayerCore(uid = "player-localization", name = "Player"))
        val team = teamCoreRepository.save(TeamCore(uid = "team-localization", name = "Team"))
        val league = leagueCoreRepository.save(LeagueCore(uid = "league-localization", name = "League"))

        playerLocalizationRepository.save(
            PlayerCoreLocalization(playerCore = player, locale = SupportedLocale.EN, name = null, shortName = null),
        )
        teamLocalizationRepository.save(
            TeamCoreLocalization(teamCore = team, locale = SupportedLocale.KO, name = "", shortName = " "),
        )
        leagueLocalizationRepository.save(
            LeagueCoreLocalization(leagueCore = league, locale = SupportedLocale.EN, name = "League EN"),
        )
        entityManager.flush()
        entityManager.clear()

        assertThat(playerLocalizationRepository.findAll().single().locale).isEqualTo(SupportedLocale.EN)
        assertThat(teamLocalizationRepository.findAll().single().name).isEmpty()
        assertThat(leagueLocalizationRepository.findAll().single().name).isEqualTo("League EN")
        assertThat(databaseLocales("player_core_localization")).containsExactly("en")
        assertThat(databaseLocales("team_core_localization")).containsExactly("ko")
        assertThat(databaseLocales("league_core_localization")).containsExactly("en")
        assertThat(databaseCoreUids("player_core_localization", "player_core_uid")).containsExactly(player.uid)
        assertThat(databaseCoreUids("team_core_localization", "team_core_uid")).containsExactly(team.uid)
        assertThat(databaseCoreUids("league_core_localization", "league_core_uid")).containsExactly(league.uid)
        assertThat(databaseAiGenerated("player_core_localization")).containsExactly(false)
        assertThat(databaseAiGenerated("team_core_localization")).containsExactly(false)
        assertThat(databaseAiGenerated("league_core_localization")).containsExactly(false)
    }

    @Test
    fun `localization repositories batch read by core uid and locale`() {
        val includedPlayer = playerCoreRepository.save(PlayerCore(uid = "included-player", name = "Player"))
        val excludedPlayer = playerCoreRepository.save(PlayerCore(uid = "excluded-player", name = "Player"))
        val includedTeam = teamCoreRepository.save(TeamCore(uid = "included-team", name = "Team"))
        val excludedTeam = teamCoreRepository.save(TeamCore(uid = "excluded-team", name = "Team"))
        val includedLeague = leagueCoreRepository.save(LeagueCore(uid = "included-league", name = "League"))
        val excludedLeague = leagueCoreRepository.save(LeagueCore(uid = "excluded-league", name = "League"))

        playerLocalizationRepository.saveAll(
            listOf(
                PlayerCoreLocalization(playerCore = includedPlayer, locale = SupportedLocale.EN, name = "Player EN"),
                PlayerCoreLocalization(playerCore = includedPlayer, locale = SupportedLocale.KO, name = "선수"),
                PlayerCoreLocalization(playerCore = excludedPlayer, locale = SupportedLocale.EN, name = "Excluded"),
            ),
        )
        teamLocalizationRepository.saveAll(
            listOf(
                TeamCoreLocalization(teamCore = includedTeam, locale = SupportedLocale.EN, name = "Team EN"),
                TeamCoreLocalization(teamCore = includedTeam, locale = SupportedLocale.KO, name = "팀"),
                TeamCoreLocalization(teamCore = excludedTeam, locale = SupportedLocale.EN, name = "Excluded"),
            ),
        )
        leagueLocalizationRepository.saveAll(
            listOf(
                LeagueCoreLocalization(leagueCore = includedLeague, locale = SupportedLocale.EN, name = "League EN"),
                LeagueCoreLocalization(leagueCore = includedLeague, locale = SupportedLocale.KO, name = "리그"),
                LeagueCoreLocalization(leagueCore = excludedLeague, locale = SupportedLocale.EN, name = "Excluded"),
            ),
        )
        entityManager.flush()
        entityManager.clear()

        val players =
            playerLocalizationRepository.findAllByCoreUidInAndLocaleIn(
                coreUids = setOf(includedPlayer.uid, "missing-player"),
                locales = SupportedLocale.entries,
            )
        val teams =
            teamLocalizationRepository.findAllByCoreUidInAndLocaleIn(
                coreUids = setOf(includedTeam.uid, "missing-team"),
                locales = setOf(SupportedLocale.KO),
            )
        val leagues =
            leagueLocalizationRepository.findAllByCoreUidInAndLocaleIn(
                coreUids = setOf(includedLeague.uid, "missing-league"),
                locales = setOf(SupportedLocale.EN),
            )

        assertThat(players.map { it.playerCore.uid to it.locale })
            .containsExactlyInAnyOrder(
                includedPlayer.uid to SupportedLocale.EN,
                includedPlayer.uid to SupportedLocale.KO,
            )
        assertThat(teams.map { it.teamCore.uid to it.locale })
            .containsExactly(includedTeam.uid to SupportedLocale.KO)
        assertThat(leagues.map { it.leagueCore.uid to it.locale })
            .containsExactly(includedLeague.uid to SupportedLocale.EN)
    }

    @Suppress("UNCHECKED_CAST")
    private fun databaseLocales(table: String): List<String> =
        entityManager.createNativeQuery("SELECT locale FROM $table ORDER BY id").resultList as List<String>

    @Suppress("UNCHECKED_CAST")
    private fun databaseCoreUids(
        table: String,
        column: String,
    ): List<String> =
        entityManager.createNativeQuery("SELECT $column FROM $table ORDER BY id").resultList as List<String>

    @Suppress("UNCHECKED_CAST")
    private fun databaseAiGenerated(table: String): List<Boolean> =
        entityManager.createNativeQuery("SELECT ai_generated FROM $table ORDER BY id").resultList as List<Boolean>
}
