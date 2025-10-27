package com.footballay.core.infra.apisports.backbone.sync.fixture

import com.footballay.core.infra.apisports.shared.dto.FixtureApiSportsSyncDto

/**
 * FixtureApiSports 동기화를 담당하는 인터페이스
 * 
 * **주요 책임:**
 * - ApiSports Fixture 데이터를 Core-Api 구조로 동기화
 * - Core-Api 연관관계 설정
 * - 기존 데이터 업데이트 및 신규 데이터 생성
 * - 경기 상태 및 스코어 정보 동기화
 */
interface FixtureApiSportsSyncer {
    
    /**
     * 특정 리그의 경기들을 동기화합니다.
     * 
     * @param leagueApiId ApiSports 리그 ID
     * @param dtos 동기화할 경기 데이터 목록 (모든 DTO는 동일한 시즌이어야 함)
     */
    fun saveFixturesOfLeague(leagueApiId: Long, dtos: List<FixtureApiSportsSyncDto>)
}