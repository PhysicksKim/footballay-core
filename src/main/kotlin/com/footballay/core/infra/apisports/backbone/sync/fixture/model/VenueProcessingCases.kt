package com.footballay.core.infra.apisports.backbone.sync.fixture.model

import com.footballay.core.infra.apisports.shared.dto.VenueOfFixtureApiSportsCreateDto
import com.footballay.core.infra.persistence.apisports.entity.VenueApiSports

/**
 * Phase 3에서 분리된 Venue 처리 케이스들을 담는 데이터 클래스
 *
 * preventUpdate 플래그를 고려하여 3가지 케이스로 분류됩니다.
 *
 * @param newVenues 새로 생성할 Venue DTO 목록
 * @param updateVenues 업데이트할 Venue 엔티티와 DTO 쌍 목록
 * @param preventUpdateVenues preventUpdate가 true인 기존 Venue 엔티티 목록 (업데이트하지 않음)
 */
data class VenueProcessingCases(
    val newVenues: List<VenueOfFixtureApiSportsCreateDto>,
    val updateVenues: List<Pair<VenueApiSports, VenueOfFixtureApiSportsCreateDto>>,
    val preventUpdateVenues: List<VenueApiSports>,
)
