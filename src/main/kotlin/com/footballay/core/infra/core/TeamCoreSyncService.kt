package com.footballay.core.infra.core

import com.footballay.core.infra.apisports.shared.dto.TeamApiSportsCreateDto
import com.footballay.core.infra.core.dto.TeamCoreCreateDto
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.TeamCore

/**
 * Team Core 저장을 담당하는 서비스 인터페이스
 *
 * **주요 책임:**
 * - TeamCore 엔티티 생성 및 저장
 * - Team-League 연관관계 설정
 * - Team 관련 트랜잭션 관리
 */
interface TeamCoreSyncService {
    /**
     * TeamCore 엔티티를 생성하고 저장합니다.
     *
     * @param dto TeamCore 생성에 필요한 데이터
     * @return 저장된 TeamCore 엔티티 (영속 상태)
     */
    fun saveTeamCore(dto: TeamCoreCreateDto): TeamCore

    /**
     * TeamApiSportsCreateDto를 사용하여 TeamCore 엔티티를 생성하고 저장합니다.
     *
     * @param dto ApiSports 팀 데이터
     * @return 저장된 TeamCore 엔티티 (영속 상태)
     */
    fun saveTeamCoreFromApiSports(dto: TeamApiSportsCreateDto): TeamCore

    /**
     * 여러 TeamApiSportsCreateDto를 배치로 처리하여 TeamCore 엔티티들을 생성하고 저장합니다.
     *
     * @param dtos ApiSports 팀 데이터 목록
     * @return 저장된 TeamCore 엔티티 목록 (영속 상태)
     */
    fun saveTeamCoresFromApiSports(dtos: List<TeamApiSportsCreateDto>): List<TeamCore>

    /**
     * TeamCore와 LeagueCore 간의 연관관계를 설정합니다.
     *
     * @param teamCore 팀 Core 엔티티
     * @param leagueCore 리그 Core 엔티티
     */
    fun createTeamLeagueRelationship(
        teamCore: TeamCore,
        leagueCore: LeagueCore,
    )

    /**
     * TeamCore 엔티티를 업데이트합니다.
     *
     * @param teamCore 업데이트할 TeamCore 엔티티
     * @param dto 업데이트에 사용할 DTO
     * @return 업데이트된 TeamCore 엔티티
     */
    fun updateTeamCore(
        teamCore: TeamCore,
        dto: TeamApiSportsCreateDto,
    ): TeamCore

    /**
     * 여러 TeamCore 엔티티를 배치로 업데이트합니다.
     *
     * @param teamCoreDtos 업데이트할 TeamCore와 DTO 쌍 목록
     * @return 업데이트된 TeamCore 엔티티 목록
     */
    fun updateTeamCores(teamCoreDtos: List<Pair<TeamCore, TeamApiSportsCreateDto>>): List<TeamCore>

    /**
     * ApiId와 TeamCoreCreateDto 쌍을 받아서 TeamCore를 생성하고 매핑을 반환합니다.
     *
     * @param apiIdTeamCorePairs ApiId와 TeamCoreCreateDto 쌍 목록
     * @return ApiId와 생성된 TeamCore 매핑
     */
    fun createTeamCoresFromApiSports(apiIdTeamCorePairs: List<Pair<Long, TeamApiSportsCreateDto>>): Map<Long, TeamCore>
}
