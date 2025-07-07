package com.footballay.core.infra.apisports.syncer.match.context

import com.footballay.core.infra.apisports.syncer.match.dto.MatchPlayerDto

/**
 * 매치 동기화 과정에서 사용되는 컨텍스트
 * 
 * - 기존 매치 데이터 캐싱 (중복 방지)
 * - 새로운 저장 요청 관리
 * - 외부 연관관계 엔티티 관리
 * 
 * DTO만 관리하여 순수한 비즈니스 로직에 집중
 */
class MatchPlayerContext() {
    val lineupMpDtoMap = mutableMapOf<String, MatchPlayerDto>()
    val eventMpDtoMap = mutableMapOf<String, MatchPlayerDto>()
    val statMpDtoMap = mutableMapOf<String, MatchPlayerDto>()

    fun putAllLineup(lineupMpDtoMap: Map<String, MatchPlayerDto>) {
        this.lineupMpDtoMap.putAll(lineupMpDtoMap)
    }

}








