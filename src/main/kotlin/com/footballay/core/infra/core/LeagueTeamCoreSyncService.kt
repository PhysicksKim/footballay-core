package com.footballay.core.infra.core

import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.TeamCore

/**
 * League-Team Core 연관관계를 담당하는 서비스 인터페이스
 *
 * **주요 책임:**
 * - League-Team 다대다 연관관계 관리
 * - 연관관계 생성, 업데이트, 삭제
 * - 다대다 관계의 무결성 보장
 */
interface LeagueTeamCoreSyncService {
    /**
     * LeagueCore와 TeamCore 간의 연관관계를 생성합니다.
     *
     * @param leagueCore 리그 Core 엔티티
     * @param teamCore 팀 Core 엔티티
     */
    fun createLeagueTeamRelationship(
        leagueCore: LeagueCore,
        teamCore: TeamCore,
    )

    /**
     * 특정 리그에 속한 팀들의 연관관계를 일괄 업데이트합니다.
     *
     * @param leagueCore 리그 Core 엔티티
     * @param teamCores 해당 리그에 속할 팀 Core 엔티티 목록
     * @param teamApiIds DTO에서 제공된 팀 API ID 목록 (제거 대상 식별용)
     */
    fun updateLeagueTeamRelationships(
        leagueCore: LeagueCore,
        teamCores: List<TeamCore>,
        teamApiIds: List<Long>,
    )

    /**
     * 특정 리그에서 팀을 제거합니다.
     *
     * @param leagueId 리그 ID
     * @param teamId 팀 ID
     */
    fun removeTeamFromLeague(
        leagueId: Long,
        teamId: Long,
    )

    /**
     * 여러 TeamCore와 LeagueCore 간의 연관관계를 배치로 생성합니다.
     *
     * @param leagueCore 리그 Core 엔티티
     * @param teamCores 팀 Core 엔티티 목록
     */
    fun createLeagueTeamRelationshipsBatch(
        leagueCore: LeagueCore,
        teamCores: Collection<TeamCore>,
    )
}
