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
    fun `localization repository maps SupportedLocale to lowercase database value`() {
        val player = playerCoreRepository.save(PlayerCore(uid = "player-localization", name = "Player"))

        playerLocalizationRepository.save(
            PlayerCoreLocalization(playerCore = player, locale = SupportedLocale.EN, name = null, shortName = null),
        )
        entityManager.flush()
        entityManager.clear()

        assertThat(playerLocalizationRepository.findAll().single().locale).isEqualTo(SupportedLocale.EN)
        assertThat(databaseLocales("player_core_localization")).containsExactly("en")
    }

    @Test
    fun `each localization repository queries by core uid and locale`() {
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
        assertThat(playerLocalizationRepository.findByCoreUidAndLocale(includedPlayer.uid, SupportedLocale.EN)?.name)
            .isEqualTo("Player EN")
        assertThat(teamLocalizationRepository.findByCoreUidAndLocale(includedTeam.uid, SupportedLocale.KO)?.name)
            .isEqualTo("팀")
        assertThat(leagueLocalizationRepository.findByCoreUidAndLocale(includedLeague.uid, SupportedLocale.EN)?.name)
            .isEqualTo("League EN")
        assertThat(playerLocalizationRepository.findByCoreUidAndLocale("missing-player", SupportedLocale.EN)).isNull()
    }

    @Suppress("UNCHECKED_CAST")
    private fun databaseLocales(table: String): List<String> =
        entityManager.createNativeQuery("SELECT locale FROM $table ORDER BY id").resultList as List<String>

}
