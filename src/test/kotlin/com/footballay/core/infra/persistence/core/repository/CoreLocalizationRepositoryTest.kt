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
    }

    @Suppress("UNCHECKED_CAST")
    private fun databaseLocales(table: String): List<String> =
        entityManager.createNativeQuery("SELECT locale FROM $table ORDER BY id").resultList as List<String>
}
