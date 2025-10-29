package com.footballay.core.infra.apisports.backbone.sync.team

import com.footballay.core.infra.apisports.shared.dto.TeamApiSportsCreateDto
import com.footballay.core.infra.persistence.apisports.entity.TeamApiSports

/**
 * TeamApiSports 동기화를 담당하는 인터페이스
 *
 * **주요 책임:**
 * - ApiSports Team 데이터를 Core-Api 구조로 동기화
 * - Core-Api 연관관계 설정
 * - 기존 데이터 업데이트 및 신규 데이터 생성
 * - 다대다 연관관계 (League-Team) 관리
 */
interface TeamApiSportsSyncer {
    /**
     * 특정 리그에 속한 팀들을 동기화합니다.
     *
     * @param leagueApiId ApiSports 리그 ID
     * @param teamDtos 동기화할 팀 데이터 목록
     * @return 저장된 TeamApiSports 엔티티 목록
     */
    fun saveTeamsOfLeague(
        leagueApiId: Long,
        teamDtos: List<TeamApiSportsCreateDto>,
    ): List<TeamApiSports>

    /**
     * 단일 팀을 특정 리그에 동기화합니다.
     *
     * @param leagueApiId ApiSports 리그 ID
     * @param teamDto 동기화할 팀 데이터
     * @return 저장된 TeamApiSports 엔티티
     */
    fun saveTeamWithLeagueId(
        leagueApiId: Long,
        teamDto: TeamApiSportsCreateDto,
    ): TeamApiSports
}
