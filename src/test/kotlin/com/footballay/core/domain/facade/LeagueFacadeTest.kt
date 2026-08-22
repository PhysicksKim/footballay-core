package com.footballay.core.domain.facade

import com.footballay.core.common.result.DomainResult
import com.footballay.core.common.result.DomainFail
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsRepository
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.LeagueTeamCore
import com.footballay.core.infra.persistence.core.entity.PlayerCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import com.footballay.core.infra.persistence.core.entity.TeamPlayerCore
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.infra.persistence.core.repository.LeagueTeamCoreRepository
import com.footballay.core.infra.persistence.core.repository.PlayerCoreRepository
import com.footballay.core.infra.persistence.core.repository.TeamCoreRepository
import com.footballay.core.infra.persistence.core.repository.TeamPlayerCoreRepository
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LeagueFacadeTest {
    @Autowired
    private lateinit var leagueFacade: LeagueFacade

    @Autowired
    private lateinit var leagueCoreRepository: LeagueCoreRepository

    @Autowired
    private lateinit var leagueApiSportsRepository: LeagueApiSportsRepository

    @Autowired
    private lateinit var teamCoreRepository: TeamCoreRepository

    @Autowired
    private lateinit var playerCoreRepository: PlayerCoreRepository

    @Autowired
    private lateinit var leagueTeamCoreRepository: LeagueTeamCoreRepository

    @Autowired
    private lateinit var teamPlayerCoreRepository: TeamPlayerCoreRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Test
    fun `core available 조회는 provider 없는 league도 반환하고 기존 조회는 제외한다`() {
        val coreOnly = leagueCoreRepository.save(LeagueCore(uid = "core-only-league", name = "Core Only", available = true))
        val providerBacked = leagueCoreRepository.save(LeagueCore(uid = "provider-league", name = "Provider", available = true))
        leagueApiSportsRepository.save(LeagueApiSports(apiId = 39L, name = "Provider", leagueCore = providerBacked))
        entityManager.flush()
        entityManager.clear()

        val coreLeagues = leagueFacade.getAvailableCoreLeagues().successValue()
        val existingLeagues = leagueFacade.getAvailableLeagues().successValue()

        assertThat(coreLeagues.map { it.uid }).contains(coreOnly.uid, providerBacked.uid)
        assertThat(existingLeagues.map { it.uid }).contains(providerBacked.uid).doesNotContain(coreOnly.uid)
    }

    @Test
    fun `league lookup과 core uid 관계 탐색을 제공한다`() {
        val league = leagueCoreRepository.save(LeagueCore(uid = "league-uid", name = "League", available = true))
        leagueApiSportsRepository.save(LeagueApiSports(apiId = 61L, name = "League", leagueCore = league))
        val team = teamCoreRepository.save(TeamCore(uid = "team-uid", name = "Team"))
        val player = playerCoreRepository.save(PlayerCore(uid = "player-uid", name = "Player"))
        leagueTeamCoreRepository.save(LeagueTeamCore(league = league, team = team))
        teamPlayerCoreRepository.save(TeamPlayerCore(team = team, player = player))

        assertThat(leagueFacade.findLeagueByUid(league.uid).successValue().name).isEqualTo("League")
        assertThat(leagueFacade.findLeagueByApiId(61L).successValue().uid).isEqualTo(league.uid)
        assertThat(leagueFacade.findTeamsByLeagueUid(league.uid).successValue().map { it.uid }).containsExactly(team.uid)
        assertThat(leagueFacade.findPlayersByTeamUid(team.uid).successValue().map { it.uid }).containsExactly(player.uid)
    }

    @Test
    fun `없는 parent core는 빈 하위 목록이 아니라 not found를 반환한다`() {
        assertThat(leagueFacade.findTeamsByLeagueUid("missing-league"))
            .isEqualTo(DomainResult.Fail(DomainFail.NotFound("LeagueCore", "missing-league")))
        assertThat(leagueFacade.findPlayersByTeamUid("missing-team"))
            .isEqualTo(DomainResult.Fail(DomainFail.NotFound("TeamCore", "missing-team")))
    }

    private fun <T : Any> DomainResult<T, *>.successValue(): T =
        (this as DomainResult.Success<T>).value
}
