package com.footballay.core.infra.apisports.syncer

import com.footballay.core.infra.persistence.core.entity.PlayerCore

interface ApiSportsNewPlayerSync : NewPlayerSync<PlayerSyncRequest> {

    /**
     * 선수 정보를 Core-Api 구조에 저장합니다.
     *
     * @param players 추출된 선수 정보 목록
     * @param teamApiId (Optional) 선수 연관관계를 맺을 팀의 API ID. null 일 경우 [com.footballay.core.infra.persistence.core.entity.TeamCore] 연관관계 맺지 않음
     * @return 제공된 [PlayerSyncRequest] 에 따라서 새롭게 저장 또는 기존에 존재했던 [com.footballay.core.infra.persistence.core.entity.PlayerCore] 목록
     */
    override fun syncPlayers(
        players: List<PlayerSyncRequest>,
        teamApiId: Long?
    ): List<PlayerCore>

    /**
     * 팀 없이 선수 정보를 Core-Api 구조에 저장합니다.
     *
     * @param players 추출된 선수 정보 목록
     * @return 새롭게 저장 또는 기존에 존재했던 [PlayerCore] 목록
     */
    override fun syncPlayerWithoutTeam(players: List<PlayerSyncRequest>): List<PlayerCore>

}