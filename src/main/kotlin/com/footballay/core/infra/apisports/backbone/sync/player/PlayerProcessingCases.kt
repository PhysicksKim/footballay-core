package com.footballay.core.infra.apisports.backbone.sync.player

import com.footballay.core.infra.apisports.backbone.sync.PlayerApiSportsCreateDto
import com.footballay.core.infra.persistence.apisports.entity.PlayerApiSports

/**
 * PlayerApiSports 엔티티 처리 시 발생하는 3가지 케이스를 관리하는 데이터 클래스
 *
 * @param bothExistPlayers 기존 PlayerApiSports와 PlayerCore가 모두 존재하는 경우 (업데이트 대상)
 * @param apiOnlyPlayers PlayerApiSports는 존재하지만 PlayerCore가 없는 경우 (Core 연결 대상)
 * @param bothNewDtos PlayerApiSports와 PlayerCore가 모두 없는 경우 (새로 생성 대상)
 */
data class PlayerProcessingCases(
    val bothExistPlayers: List<PlayerApiSports>,
    val apiOnlyPlayers: List<Pair<PlayerApiSports, PlayerApiSportsCreateDto>>,
    val bothNewDtos: List<PlayerApiSportsCreateDto>
)
