package com.footballay.core.infra.apisports.match.sync.context

import com.footballay.core.infra.apisports.match.sync.dto.MatchPlayerDto

/**
 * 매치 동기화 과정에서 수집된 MatchPlayer DTO들을 출처별로 관리하는 컨텍스트
 *
 * 라인업, 이벤트, 통계 등 여러 출처에서 등장하는 선수 데이터를 구분하여 보관하고,
 * 중복 제거 및 선수 정보 통합 작업에 활용됩니다.
 */
class MatchPlayerContext {
    val lineupMpDtoMap = mutableMapOf<String, MatchPlayerDto>()
    val eventMpDtoMap = mutableMapOf<String, MatchPlayerDto>()
    val statMpDtoMap = mutableMapOf<String, MatchPlayerDto>()

    internal fun putAllLineup(lineupMpDtoMap: Map<String, MatchPlayerDto>) {
        this.lineupMpDtoMap.putAll(lineupMpDtoMap)
    }
}








