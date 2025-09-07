package com.footballay.core.infra.apisports.backbone.sync.team

import com.footballay.core.infra.apisports.shared.dto.TeamApiSportsCreateDto
import com.footballay.core.infra.persistence.apisports.entity.TeamApiSports

/**
 * TeamApiSports 엔티티 처리 시 발생하는 3가지 케이스를 관리하는 데이터 클래스
 *
 * @param bothExistTeams 기존 TeamApiSports와 TeamCore가 모두 존재하는 경우 (업데이트 대상)
 * @param apiOnlyTeams TeamApiSports는 존재하지만 TeamCore가 없는 경우 (Core 연결 대상)
 * @param bothNewDtos TeamApiSports와 TeamCore가 모두 없는 경우 (새로 생성 대상)
 */
data class ProcessingCases(
    val bothExistTeams: List<TeamApiSports>,
    val apiOnlyTeams: List<Pair<TeamApiSports, TeamApiSportsCreateDto>>,
    val bothNewDtos: List<TeamApiSportsCreateDto>
)
