package com.footballay.core.infra.persistence.core.entity

import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.infra.persistence.core.repository.TeamCoreRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * 리그-팀 다대다 연관관계 메서드 테스트
 */
@SpringBootTest
@ActiveProfiles("dev","devrealapi")
class LeagueTeamRelationTest {

    @Autowired
    private lateinit var relationTestHelpServices: RelationTestHelpServices

    @Autowired
    private lateinit var leagueCoreRepository: LeagueCoreRepository

    @Autowired
    private lateinit var teamCoreRepository: TeamCoreRepository

    private lateinit var league1: LeagueCore
    private lateinit var league2: LeagueCore
    private lateinit var team1: TeamCore
    private lateinit var team2: TeamCore

    @BeforeEach
    fun setup() {
        // 기존 데이터 삭제
        leagueCoreRepository.deleteAll()
        teamCoreRepository.deleteAll()

        // 테스트 데이터 생성
        league1 = leagueCoreRepository.save(LeagueCore(uid = "league1", name = "Premier League"))
        league2 = leagueCoreRepository.save(LeagueCore(uid = "league2", name = "La Liga"))
        team1 = teamCoreRepository.save(TeamCore(uid = "team1", name = "Manchester United"))
        team2 = teamCoreRepository.save(TeamCore(uid = "team2", name = "Real Madrid"))
    }

    @Test
    fun `리그에 팀 추가 테스트`() {
        // given
        val leagueId = league1.id!!
        val teamId = team1.id!!

        // when - 리그에 팀 추가
        relationTestHelpServices.addTeamToLeague(leagueId, teamId)

        // then - 별도 트랜잭션에서 관계 검증
        val result = relationTestHelpServices.verifyRelationship(leagueId, teamId)
        assertThat(result).isTrue()

        // 리그에 속한 팀 목록 조회 검증
        val teamsInLeague = relationTestHelpServices.getTeamsInLeague(leagueId)
        assertThat(teamsInLeague).hasSize(1)
        assertThat(teamsInLeague[0].id).isEqualTo(teamId)
    }

    @Test
    fun `팀에 리그 추가 테스트`() {
        // given
        val teamId = team2.id!!
        val leagueId = league2.id!!

        // when - 팀에 리그 추가
        relationTestHelpServices.addLeagueToTeam(teamId, leagueId)

        // then - 별도 트랜잭션에서 관계 검증
        val result = relationTestHelpServices.verifyRelationship(leagueId, teamId)
        assertThat(result).isTrue()

        // 팀이 속한 리그 목록 조회 검증
        val leaguesForTeam = relationTestHelpServices.getLeaguesForTeam(teamId)
        assertThat(leaguesForTeam).hasSize(1)
        assertThat(leaguesForTeam[0].id).isEqualTo(leagueId)
    }

    @Test
    fun `양방향 관계 일관성 테스트`() {
        // given
        val leagueId = league1.id!!
        val teamId = team2.id!!

        // when - 리그에 팀 추가
        relationTestHelpServices.addTeamToLeague(leagueId, teamId)

        // then - 팀 측에서도 리그가 등록되어 있어야 함
        val leaguesForTeam = relationTestHelpServices.getLeaguesForTeam(teamId)
        assertThat(leaguesForTeam).hasSize(1)
        assertThat(leaguesForTeam[0].id).isEqualTo(leagueId)
    }

    @Test
    fun `리그에서 팀 제거 테스트`() {
        // given - 먼저 연관관계 설정
        val leagueId = league1.id!!
        val teamId = team1.id!!
        relationTestHelpServices.addTeamToLeague(leagueId, teamId)

        // 연관관계가 설정되었는지 확인
        assertThat(relationTestHelpServices.verifyRelationship(leagueId, teamId)).isTrue()

        // when - 리그에서 팀 제거
        relationTestHelpServices.removeTeamFromLeague(leagueId, teamId)

        // then - 연관관계가 제거되었는지 확인
        assertThat(relationTestHelpServices.verifyRelationship(leagueId, teamId)).isFalse()
        assertThat(relationTestHelpServices.getTeamsInLeague(leagueId)).isEmpty()
    }

    @Test
    fun `팀에서 리그 제거 테스트`() {
        // given - 먼저 연관관계 설정
        val teamId = team2.id!!
        val leagueId = league2.id!!
        relationTestHelpServices.addLeagueToTeam(teamId, leagueId)

        // 연관관계가 설정되었는지 확인
        assertThat(relationTestHelpServices.verifyRelationship(leagueId, teamId)).isTrue()

        // when - 팀에서 리그 제거
        relationTestHelpServices.removeLeagueFromTeam(teamId, leagueId)

        // then - 연관관계가 제거되었는지 확인
        assertThat(relationTestHelpServices.verifyRelationship(leagueId, teamId)).isFalse()
        assertThat(relationTestHelpServices.getLeaguesForTeam(teamId)).isEmpty()
    }

    @Test
    fun `여러 리그에 팀 추가 테스트`() {
        // given
        val teamId = team1.id!!
        val league1Id = league1.id!!
        val league2Id = league2.id!!

        // when - 팀에 여러 리그 추가
        relationTestHelpServices.addLeagueToTeam(teamId, league1Id)
        relationTestHelpServices.addLeagueToTeam(teamId, league2Id)

        // then - 팀이 여러 리그에 속해 있는지 확인
        val leaguesForTeam = relationTestHelpServices.getLeaguesForTeam(teamId)
        assertThat(leaguesForTeam).hasSize(2)
        assertThat(leaguesForTeam.map { it.id }).contains(league1Id, league2Id)
    }

    @Test
    fun `리그에 여러 팀 추가 테스트`() {
        // given
        val leagueId = league1.id!!
        val team1Id = team1.id!!
        val team2Id = team2.id!!

        // when - 리그에 여러 팀 추가
        relationTestHelpServices.addTeamToLeague(leagueId, team1Id)
        relationTestHelpServices.addTeamToLeague(leagueId, team2Id)

        // then - 리그에 여러 팀이 속해 있는지 확인
        val teamsInLeague = relationTestHelpServices.getTeamsInLeague(leagueId)
        assertThat(teamsInLeague).hasSize(2)
        assertThat(teamsInLeague.map { it.id }).contains(team1Id, team2Id)
    }
}
