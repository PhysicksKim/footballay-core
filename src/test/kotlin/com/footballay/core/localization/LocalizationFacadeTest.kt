package com.footballay.core.localization

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.PlayerCore
import com.footballay.core.infra.persistence.core.entity.PlayerCoreLocalization
import com.footballay.core.infra.persistence.core.entity.TeamCore
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.infra.persistence.core.repository.PlayerCoreLocalizationRepository
import com.footballay.core.infra.persistence.core.repository.PlayerCoreRepository
import com.footballay.core.infra.persistence.core.repository.TeamCoreRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LocalizationFacadeTest {
    @Autowired
    private lateinit var localizationFacade: LocalizationFacade

    @Autowired
    private lateinit var leagueCoreRepository: LeagueCoreRepository

    @Autowired
    private lateinit var teamCoreRepository: TeamCoreRepository

    @Autowired
    private lateinit var playerCoreRepository: PlayerCoreRepository

    @Autowired
    private lateinit var playerLocalizationRepository: PlayerCoreLocalizationRepository

    @Test
    fun `manual upsert는 entity별 localization을 생성하고 ai generated를 false로 저장한다`() {
        val league = leagueCoreRepository.save(LeagueCore(uid = "localization-league", name = "League"))
        val team = teamCoreRepository.save(TeamCore(uid = "localization-team", name = "Team"))
        val player = playerCoreRepository.save(PlayerCore(uid = "localization-player", name = "Player"))

        val leagueLocalization = localizationFacade.upsertLeagueLocalization(league.uid, SupportedLocale.KO, "리그", null).successValue().localization
        val teamLocalization = localizationFacade.upsertTeamLocalization(team.uid, SupportedLocale.KO, null, "팀").successValue().localization
        val playerLocalization = localizationFacade.upsertPlayerLocalization(player.uid, SupportedLocale.KO, "선수", "선수").successValue().localization

        assertThat(leagueLocalization).isEqualTo(
            CoreLocalizationModel(league.uid, SupportedLocale.KO, "리그", null, false),
        )
        assertThat(teamLocalization).isEqualTo(
            CoreLocalizationModel(team.uid, SupportedLocale.KO, null, "팀", false),
        )
        assertThat(playerLocalization).isEqualTo(
            CoreLocalizationModel(player.uid, SupportedLocale.KO, "선수", "선수", false),
        )
    }

    @Test
    fun `manual upsert는 non null field만 변경하고 no op은 ai generated를 유지한다`() {
        val player = playerCoreRepository.save(PlayerCore(uid = "existing-player", name = "Player"))
        playerLocalizationRepository.save(
            PlayerCoreLocalization(
                playerCore = player,
                locale = SupportedLocale.KO,
                name = "기존 이름",
                shortName = "기존 약칭",
                aiGenerated = true,
            ),
        )

        val nameOnly = localizationFacade.upsertPlayerLocalization(player.uid, SupportedLocale.KO, "수정 이름", null).successValue().localization
        val noOp = localizationFacade.upsertPlayerLocalization(player.uid, SupportedLocale.KO, null, null).successValue().localization

        assertThat(nameOnly).isEqualTo(
            CoreLocalizationModel(player.uid, SupportedLocale.KO, "수정 이름", "기존 약칭", false),
        )
        assertThat(noOp).isEqualTo(nameOnly)
    }

    @Test
    fun `missing localization은 값이 없으면 생성하지 않고 batch와 단건 조회를 제공한다`() {
        val player = playerCoreRepository.save(PlayerCore(uid = "missing-player", name = "Player"))

        assertThat(localizationFacade.upsertPlayerLocalization(player.uid, SupportedLocale.KO, null, null).successValue().localization).isNull()
        assertThat(localizationFacade.findPlayerLocalization(player.uid, SupportedLocale.KO)).isNull()

        localizationFacade.upsertPlayerLocalization(player.uid, SupportedLocale.KO, "선수", null).successValue()

        assertThat(localizationFacade.findPlayerLocalization(player.uid, SupportedLocale.KO)).isEqualTo(
            CoreLocalizationModel(player.uid, SupportedLocale.KO, "선수", null, false),
        )
        assertThat(localizationFacade.findPlayerLocalizations(setOf(player.uid), setOf(SupportedLocale.KO)))
            .containsExactly(CoreLocalizationModel(player.uid, SupportedLocale.KO, "선수", null, false))
    }

    @Test
    fun `없는 core localization upsert는 not found를 반환한다`() {
        assertThat(localizationFacade.upsertLeagueLocalization("missing-league", SupportedLocale.KO, null, null))
            .isEqualTo(DomainResult.Fail(DomainFail.NotFound("LeagueCore", "missing-league")))
        assertThat(localizationFacade.upsertTeamLocalization("missing-team", SupportedLocale.KO, null, null))
            .isEqualTo(DomainResult.Fail(DomainFail.NotFound("TeamCore", "missing-team")))
        assertThat(localizationFacade.upsertPlayerLocalization("missing-player", SupportedLocale.KO, null, null))
            .isEqualTo(DomainResult.Fail(DomainFail.NotFound("PlayerCore", "missing-player")))
    }

    private fun <T : Any> DomainResult<T, *>.successValue(): T =
        (this as DomainResult.Success<T>).value
}
