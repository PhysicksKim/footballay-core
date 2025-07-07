package com.footballay.core.infra.apisports.syncer

import com.footballay.core.infra.persistence.core.entity.PlayerCore
import com.footballay.core.infra.persistence.core.entity.TeamPlayerCore
import com.footballay.core.infra.persistence.core.entity.TeamCore

/**
 * 새롭게 등장한 선수를 Core-Api 연관관계와 함께 저장하는 인터페이스입니다. <br>
 * Core-Api 연관관계와 더불어 Player-Team 연관관계도 처리해야 합니다.  <br>
 *
 * 구현체에서는 다음 사항들을 반드시 처리해야 합니다:
 * - [PlayerCore] ↔ *PlayerSomeApiProvider* 연관관계
 * - [TeamCore] ↔ [PlayerCore] 연관관계 (다대다 맵핑을 위한 [TeamPlayerCore] 엔티티 사용)
 * - 기존 엔티티 존재 여부 확인에 따른 분기 처리
 * - (Optional) 선수 정보가 이미 존재하는 경우 업데이트 처리
 */
interface NewPlayerSync <SyncRequest> {
    
    /**
     * 선수 정보를 Core-Api 구조에 저장합니다.
     * 
     * @param players 추출된 선수 정보 목록
     * @param teamApiId (Optional) 선수 연관관계를 맺을 팀의 API ID. null 일 경우 [TeamCore] 연관관계 맺지 않음
     * @return 제공된 [SyncRequest] 에 따라서 새롭게 저장 또는 기존에 존재했던 [PlayerCore] 목록
     */
    fun syncPlayers(
        players: List<SyncRequest>,
        teamApiId: Long? = null
    ): List<PlayerCore>

    fun syncPlayerWithoutTeam(
        players: List<SyncRequest>
    ): List<PlayerCore>


}