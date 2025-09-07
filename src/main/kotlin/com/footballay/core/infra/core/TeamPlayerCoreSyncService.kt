package com.footballay.core.infra.core

import com.footballay.core.infra.persistence.core.entity.PlayerCore
import com.footballay.core.infra.persistence.core.entity.TeamCore

/**
 * Team-Player 다대다 관계 관리를 위한 서비스 인터페이스
 * 
 * TeamCore와 PlayerCore 간의 연관관계를 효율적으로 관리합니다.
 */
interface TeamPlayerCoreSyncService {
    
    /**
     * TeamCore와 PlayerCore 간의 연관관계를 생성합니다.
     * 
     * @param teamCore TeamCore 엔티티
     * @param playerCore PlayerCore 엔티티
     */
    fun createTeamPlayerRelationship(teamCore: TeamCore, playerCore: PlayerCore)
    
    /**
     * TeamCore와 PlayerCore들 간의 연관관계를 배치로 생성합니다.
     * 
     * @param teamCore TeamCore 엔티티
     * @param playerCores PlayerCore 엔티티 목록
     */
    fun createTeamPlayerRelationshipsBatch(teamCore: TeamCore, playerCores: List<PlayerCore>)
    
    /**
     * TeamCore와 PlayerCore들 간의 연관관계를 업데이트합니다.
     * 
     * @param teamCore TeamCore 엔티티
     * @param playerCores PlayerCore 엔티티 목록
     * @param playerApiIds Player API ID 목록
     */
    fun updateTeamPlayerRelationships(teamCore: TeamCore, playerCores: List<PlayerCore>, playerApiIds: List<Long>)
    
    /**
     * 팀에서 선수를 제거합니다.
     * 
     * @param teamId 팀 ID
     * @param playerId 선수 ID
     */
    fun removePlayerFromTeam(teamId: Long, playerId: Long)
} 