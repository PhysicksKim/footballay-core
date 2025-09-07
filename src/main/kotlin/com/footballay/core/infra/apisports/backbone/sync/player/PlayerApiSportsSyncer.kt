package com.footballay.core.infra.apisports.backbone.sync.player

import com.footballay.core.infra.apisports.backbone.sync.ApiSportsNewPlayerSync
import com.footballay.core.infra.apisports.backbone.sync.PlayerApiSportsCreateDto
import com.footballay.core.infra.persistence.apisports.entity.PlayerApiSports
import com.footballay.core.infra.persistence.apisports.repository.PlayerApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.TeamApiSportsRepository
import com.footballay.core.infra.persistence.core.entity.PlayerCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import com.footballay.core.infra.persistence.core.entity.TeamPlayerCore
import com.footballay.core.infra.persistence.core.repository.PlayerCoreRepository
import com.footballay.core.infra.persistence.core.repository.TeamPlayerCoreRepository
import com.footballay.core.infra.util.UidGenerator
import com.footballay.core.logger
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import kotlin.collections.get

/**
 * PlayerApiSports 엔티티 동기화를 위한 인터페이스
 * 
 * 팀의 선수들을 배치로 처리하여 PlayerApiSports와 PlayerCore 엔티티를 동기화합니다.
 * Team-Player 다대다 관계도 함께 처리합니다.
 */
interface PlayerApiSportsSyncer {
    
    /**
     * 팀의 선수들을 동기화합니다.
     * 
     * @param teamApiId 팀의 API ID
     * @param dtos 동기화할 선수 DTO 목록
     * @return 처리된 PlayerApiSports 엔티티 목록
     */
    fun syncPlayersOfTeam(teamApiId: Long, dtos: List<PlayerApiSportsCreateDto>): List<PlayerApiSports>
    
    /**
     * 팀 연관관계 없이 선수들을 동기화합니다.
     * 
     * @param dtos 동기화할 선수 DTO 목록
     * @return 처리된 PlayerCore 엔티티 목록
     */
    fun syncPlayerWithoutTeam(dtos: List<PlayerApiSportsCreateDto>): List<PlayerCore>
}
