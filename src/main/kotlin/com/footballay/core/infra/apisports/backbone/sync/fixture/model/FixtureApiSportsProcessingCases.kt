package com.footballay.core.infra.apisports.backbone.sync.fixture.model

import com.footballay.core.infra.apisports.shared.dto.FixtureApiSportsSyncDto
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports

/**
 * Phase 6에서 분리된 FixtureApiSports 처리 케이스들을 담는 데이터 클래스
 * 
 * preventUpdate 플래그를 고려하여 3가지 케이스로 분류됩니다.
 * 
 * @param newFixtures 새로 생성할 FixtureApiSports DTO 목록
 * @param updateFixtures 업데이트할 FixtureApiSports 엔티티와 DTO 쌍 목록
 * @param preventUpdateFixtures preventUpdate가 true인 기존 FixtureApiSports 엔티티 목록 (업데이트하지 않음)
 */
data class FixtureApiSportsProcessingCases(
    val newFixtures: List<FixtureApiSportsSyncDto>,
    val updateFixtures: List<Pair<FixtureApiSports, FixtureApiSportsSyncDto>>,
    val preventUpdateFixtures: List<FixtureApiSports>
)
