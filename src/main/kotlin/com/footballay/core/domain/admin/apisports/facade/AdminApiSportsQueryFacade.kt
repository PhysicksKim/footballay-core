package com.footballay.core.domain.admin.apisports.facade

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.admin.apisports.mapper.DomainModelMapper
import com.footballay.core.domain.admin.apisports.model.PlayerModel
import com.footballay.core.domain.admin.apisports.model.TeamModel
import com.footballay.core.infra.persistence.apisports.repository.PlayerApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.TeamApiSportsRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Admin API - ApiSports 조회 Facade (Aggregate Root)
 *
 * ApiSports provider의 팀/선수 데이터를 조회하여 Domain Model로 변환합니다.
 * Repository에서 조회한 엔티티를 DomainModelMapper를 통해 Domain Model로 변환하고 DomainResult로 래핑합니다.
 */
@Component
@Transactional(readOnly = true)
class AdminApiSportsQueryFacade(
    private val teamApiSportsRepository: TeamApiSportsRepository,
    private val playerApiSportsRepository: PlayerApiSportsRepository,
    private val domainModelMapper: DomainModelMapper,
) {
    /**
     * LeagueApiSports apiId로 해당 리그의 팀 목록 조회
     *
     * **사용 시나리오:**
     * 1. Admin이 리그의 팀 sync 완료 후 결과 확인
     * 2. Players sync를 위한 팀 선택 시 드롭다운 목록 제공
     *
     * @param leagueApiId LeagueApiSports의 apiId (예: 39 = Premier League)
     * @return DomainResult<List<TeamModel>, DomainFail>
     */
    fun findTeamsByLeagueApiId(leagueApiId: Long): DomainResult<List<TeamModel>, DomainFail> {
        val teamApiSportsList = teamApiSportsRepository.findAllByLeagueApiSportsApiId(leagueApiId)

        val teams = teamApiSportsList.map { domainModelMapper.toTeamModel(it) }

        return DomainResult.Success(teams)
    }

    /**
     * TeamApiSports apiId로 해당 팀의 선수 목록 조회
     *
     * **사용 시나리오:**
     * 1. Admin이 팀의 선수 sync 완료 후 결과 확인
     * 2. 선수 데이터 검증 및 관리
     *
     * @param teamApiId TeamApiSports의 apiId (예: 50 = Manchester City)
     * @return DomainResult<List<PlayerModel>, DomainFail>
     */
    fun findPlayersByTeamApiId(teamApiId: Long): DomainResult<List<PlayerModel>, DomainFail> {
        val playerApiSportsList = playerApiSportsRepository.findAllByTeamApiSportsApiId(teamApiId)

        val players = playerApiSportsList.map { domainModelMapper.toPlayerModel(it) }

        return DomainResult.Success(players)
    }
}
