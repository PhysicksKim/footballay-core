package com.footballay.core.infra.apisports.backbone.sync.fixture.model

import com.footballay.core.infra.apisports.shared.dto.FixtureApiSportsSyncDto
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports

/**
 * Phase 4에서 분리된 Fixture 처리 케이스들을 담는 데이터 클래스
 * 
 * FixtureApiSports와 FixtureCore의 존재 여부에 따라 3가지 케이스로 분류됩니다.
 * 
 * @param bothExistFixtures FixtureApiSports와 FixtureCore가 모두 존재하는 경우 (업데이트 대상)
 * @param apiOnlyFixtures FixtureApiSports는 존재하지만 FixtureCore가 없는 경우 (Core 연결 대상)
 * @param bothNewDtos FixtureApiSports와 FixtureCore가 모두 없는 경우 (새로 생성 대상)
 */
data class FixtureProcessingCases(
    val bothExistFixtures: List<FixtureWithDto>,
    val apiOnlyFixtures: List<FixtureWithDto>,
    val bothNewDtos: List<FixtureApiSportsSyncDto>
)

data class FixtureWithDto(
    val entity: FixtureApiSports,
    val dto: FixtureApiSportsSyncDto,
)
