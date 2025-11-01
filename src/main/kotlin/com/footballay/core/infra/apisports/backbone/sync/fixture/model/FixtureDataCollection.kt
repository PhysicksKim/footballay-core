package com.footballay.core.infra.apisports.backbone.sync.fixture.model

import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import com.footballay.core.infra.persistence.apisports.entity.TeamApiSports

/**
 * Fixture 동기화에 필요한 데이터를 담는 데이터 클래스
 *
 * @param league ApiSports 리그 정보 (Core 정보 포함)
 * @param fixtures 기존 FixtureApiSports 엔티티 목록
 * @param teams ApiSports 팀 정보 목록 (Core 정보 포함)
 *
 * ## Eager Fetch 연관관계
 * - `league.leagueCore` (Eager)
 * - `league.seasons` (Eager)
 * - `teams[*].teamCore` (Eager)
 * - `fixtures[*].core` (조회 시점에 따라 다름)
 * - `fixtures[*].venue` (조회 시점에 따라 다름)
 *
 * @see FixtureApiSportsWithCoreSyncer.collectFixtureData
 */
data class FixtureDataCollection(
    val league: LeagueApiSports,
    val fixtures: List<FixtureApiSports>,
    val teams: List<TeamApiSports>,
)
