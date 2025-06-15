package com.footballay.core.infra.persistence.core.entity

import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.infra.persistence.core.repository.LeagueTeamCoreRepository
import com.footballay.core.infra.persistence.core.repository.TeamCoreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 연관관계 테스트를 위한 헬퍼 서비스
 * 트랜잭션 경계를 명시적으로 관리하여 1차 캐시 문제 없이 연관관계를 테스트할 수 있음
 */
@Service
class RelationTestHelpServices(
    private val leagueCoreRepository: LeagueCoreRepository,
    private val teamCoreRepository: TeamCoreRepository,
    private val leagueTeamCoreRepository: LeagueTeamCoreRepository
) {

    /**
     * 리그에 팀 추가하기
     */
    @Transactional
    fun addTeamToLeague(leagueId: Long, teamId: Long) {
        val league = leagueCoreRepository.findById(leagueId).orElseThrow { IllegalArgumentException("League not found with id: $leagueId") }
        val team = teamCoreRepository.findById(teamId).orElseThrow { IllegalArgumentException("Team not found with id: $teamId") }

        league.addTeam(team)
        leagueCoreRepository.save(league)
    }

    /**
     * 리그에서 팀 제거하기
     */
    @Transactional
    fun removeTeamFromLeague(leagueId: Long, teamId: Long) {
        val league = leagueCoreRepository.findById(leagueId).orElseThrow { IllegalArgumentException("League not found with id: $leagueId") }
        val team = teamCoreRepository.findById(teamId).orElseThrow { IllegalArgumentException("Team not found with id: $teamId") }

        // 연관 관계 객체들 찾기 (중복된 관계가 있을 수 있음)
        val leagueTeams = leagueTeamCoreRepository.findByLeagueIdAndTeamId(leagueId, teamId)

        if (leagueTeams.isNotEmpty()) {
            // 모든 관계 제거
            leagueTeams.forEach { leagueTeam ->
                // 양쪽 엔티티에서 관계 제거
                league.leagueTeams.remove(leagueTeam)
                team.leagueTeams.remove(leagueTeam)

                // 직접 레포지토리를 통해 연관관계 테이블 레코드 삭제
                leagueTeamCoreRepository.delete(leagueTeam)
            }

            // 엔티티 상태 저장
            leagueCoreRepository.save(league)
            teamCoreRepository.save(team)
        }
    }

    /**
     * 팀에 리그 추가하기
     */
    @Transactional
    fun addLeagueToTeam(teamId: Long, leagueId: Long) {
        val team = teamCoreRepository.findById(teamId).orElseThrow { IllegalArgumentException("Team not found with id: $teamId") }
        val league = leagueCoreRepository.findById(leagueId).orElseThrow { IllegalArgumentException("League not found with id: $leagueId") }

        team.addLeague(league)
        teamCoreRepository.save(team)
    }

    /**
     * 팀에서 리그 제거하기
     */
    @Transactional
    fun removeLeagueFromTeam(teamId: Long, leagueId: Long) {
        val team = teamCoreRepository.findById(teamId).orElseThrow { IllegalArgumentException("Team not found with id: $teamId") }
        val league = leagueCoreRepository.findById(leagueId).orElseThrow { IllegalArgumentException("League not found with id: $leagueId") }

        // 연관 관계 객체들 찾기 (중복된 관계가 있을 수 있음)
        val leagueTeams = leagueTeamCoreRepository.findByLeagueIdAndTeamId(leagueId, teamId)

        if (leagueTeams.isNotEmpty()) {
            // 모든 관계 제거
            leagueTeams.forEach { leagueTeam ->
                // 양쪽 엔티티에서 관계 제거
                team.leagueTeams.remove(leagueTeam)
                league.leagueTeams.remove(leagueTeam)

                // 직접 레포지토리를 통해 연관관계 테이블 레코드 삭제
                leagueTeamCoreRepository.delete(leagueTeam)
            }

            // 엔티티 상태 저장
            teamCoreRepository.save(team)
            leagueCoreRepository.save(league)
        }
    }

    /**
     * 리그에 속한 팀 목록 조회 (새 트랜잭션에서 조회)
     */
    @Transactional(readOnly = true)
    fun getTeamsInLeague(leagueId: Long): List<TeamCore> {
        val league = leagueCoreRepository.findById(leagueId).orElseThrow { IllegalArgumentException("League not found with id: $leagueId") }
        return league.leagueTeams.mapNotNull { it.team }.toList()
    }

    /**
     * 팀이 속한 리그 목록 조회 (새 트랜잭션에서 조회)
     */
    @Transactional(readOnly = true)
    fun getLeaguesForTeam(teamId: Long): List<LeagueCore> {
        val team = teamCoreRepository.findById(teamId).orElseThrow { IllegalArgumentException("Team not found with id: $teamId") }
        return team.getLeagues().toList()
    }

    /**
     * 연관관계 검증을 위한 메서드
     * 리그와 팀 간의 연관관계가 올바르게 설정되었는지 확인
     */
    @Transactional(readOnly = true)
    fun verifyRelationship(leagueId: Long, teamId: Long): Boolean {
        val league = leagueCoreRepository.findById(leagueId).orElseThrow { IllegalArgumentException("League not found with id: $leagueId") }
        val team = teamCoreRepository.findById(teamId).orElseThrow { IllegalArgumentException("Team not found with id: $teamId") }

        val teamInLeague = league.leagueTeams.any { it.team?.id == teamId }
        val leagueInTeam = team.leagueTeams.any { it.league?.id == leagueId }

        // 연관관계 테이블에서도 직접 확인
        val existsInJoinTable = leagueTeamCoreRepository.existsByLeagueIdAndTeamId(leagueId, teamId)

        return teamInLeague && leagueInTeam && existsInJoinTable
    }

    /**
     * 직접 연관관계 테이블을 통해 관계가 존재하는지 확인
     * 엔티티 캐시와 무관하게 DB에서 직접 확인
     */
    @Transactional(readOnly = true)
    fun checkRelationshipInDatabase(leagueId: Long, teamId: Long): Boolean {
        return leagueTeamCoreRepository.existsByLeagueIdAndTeamId(leagueId, teamId)
    }
}
