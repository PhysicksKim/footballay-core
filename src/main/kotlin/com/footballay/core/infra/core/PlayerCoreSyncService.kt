package com.footballay.core.infra.core

import com.footballay.core.infra.apisports.backbone.sync.PlayerApiSportsCreateDto
import com.footballay.core.infra.persistence.core.entity.PlayerCore

/**
 * PlayerCore 엔티티 동기화를 위한 서비스 인터페이스
 *
 * PlayerCore 엔티티의 생성, 업데이트, 배치 처리를 담당합니다.
 */
interface PlayerCoreSyncService {
    /**
     * 단일 PlayerCore를 생성합니다.
     *
     * @param dto PlayerCore 생성에 필요한 DTO
     * @return 생성된 PlayerCore 엔티티
     */
    fun savePlayerCore(dto: PlayerApiSportsCreateDto): PlayerCore

    /**
     * ApiSports DTO로부터 PlayerCore를 생성합니다.
     *
     * @param dto ApiSports DTO
     * @return 생성된 PlayerCore 엔티티
     */
    fun savePlayerCoreFromApiSports(dto: PlayerApiSportsCreateDto): PlayerCore

    /**
     * ApiSports DTO 목록으로부터 PlayerCore들을 배치로 생성합니다.
     *
     * @param dtos ApiSports DTO와 apiId의 쌍 목록
     * @return 생성된 PlayerCore 맵 (apiId -> PlayerCore)
     */
    fun createPlayerCoresFromApiSports(dtos: List<Pair<Long, PlayerApiSportsCreateDto>>): Map<Long, PlayerCore>

    /**
     * PlayerCore를 업데이트합니다.
     *
     * @param playerCore 업데이트할 PlayerCore 엔티티
     * @param dto 업데이트에 사용할 DTO
     * @return 업데이트된 PlayerCore 엔티티
     */
    fun updatePlayerCore(
        playerCore: PlayerCore,
        dto: PlayerApiSportsCreateDto,
    ): PlayerCore

    /**
     * PlayerCore들을 배치로 업데이트합니다.
     *
     * @param playerCoreDtos PlayerCore와 DTO의 쌍 목록
     * @return 업데이트된 PlayerCore 엔티티 목록
     */
    fun updatePlayerCores(playerCoreDtos: List<Pair<PlayerCore, PlayerApiSportsCreateDto>>): List<PlayerCore>
}
