package com.footballay.core.infra.apisports.backbone.sync.league

import com.footballay.core.infra.apisports.shared.dto.LeagueApiSportsCreateDto

/**
 * LeagueApiSports 동기화를 담당하는 인터페이스
 *
 * **주요 책임:**
 * - ApiSports League 데이터를 Core-Api 구조로 동기화
 * - Core-Api 연관관계 설정
 * - 기존 데이터 업데이트 및 신규 데이터 생성
 */
interface LeagueApiSportsSyncer {
    /**
     * LeagueApiSports 데이터를 Core-Api 구조로 동기화합니다.
     *
     * @param dtos 동기화할 LeagueApiSports 데이터 목록
     */
    fun saveLeagues(dtos: List<LeagueApiSportsCreateDto>)
}
