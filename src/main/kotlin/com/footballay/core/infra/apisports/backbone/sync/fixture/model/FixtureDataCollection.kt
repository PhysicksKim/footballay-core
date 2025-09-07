package com.footballay.core.infra.apisports.backbone.sync.fixture.model

import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import com.footballay.core.infra.persistence.apisports.entity.TeamApiSports

/**
 * Phase 2에서 수집된 Fixture 동기화에 필요한 데이터를 담는 데이터 클래스
 * 
 * @param league ApiSports 리그 정보 (Core 정보 포함)
 * @param fixtures 기존 FixtureApiSports 엔티티 목록
 * @param teams ApiSports 팀 정보 목록 (Core 정보 포함)
 *
 * @see com.footballay.core.infra.apisports.backbone.sync.fixture.service.FixtureSyncService
 */
data class FixtureDataCollection(
    val league: LeagueApiSports,
    val fixtures: List<FixtureApiSports>,
    val teams: List<TeamApiSports>
)
