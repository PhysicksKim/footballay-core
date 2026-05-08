package com.footballay.core.cache.matchdata.polling.hash

import com.footballay.core.web.football.dto.FixtureEventsResponse
import com.footballay.core.web.football.dto.FixtureLineupResponse
import com.footballay.core.web.football.dto.FixtureLiveStatusResponse
import com.footballay.core.web.football.dto.FixtureStatisticsResponse

/**
 * fixture 응답 DTO로부터 캐시 저장용 표현물을 생성합니다.
 *
 * 이 타입의 책임은 아래 3가지를 함께 만드는 것까지입니다.
 *
 * - 사용자 응답용 `snapshotJson`
 * - canonical JSON 기준 `etagHash`
 *
 * 반대로 이 타입은 저장소 위치를 알지 않습니다.
 * 즉 cache key 생성, Redis key naming, TTL 결정, 저장/조회 자체는
 * 이 factory 바깥의 별도 계층이 책임집니다.
 *
 * 또한 HTTP `ETag` 헤더 포맷(`W/"..."`)이나 `If-None-Match` 비교는
 * 이 factory 바깥의 별도 helper 가 책임집니다.
 */
interface FixtureResponseCacheDocumentFactory {
    fun create(response: FixtureLiveStatusResponse): FixtureResponseCacheDocument

    fun create(response: FixtureEventsResponse): FixtureResponseCacheDocument

    fun create(response: FixtureLineupResponse): FixtureResponseCacheDocument

    fun create(response: FixtureStatisticsResponse): FixtureResponseCacheDocument
}

/**
 * 캐시에 저장할 값 묶음입니다.
 *
 * 이 객체는 "무엇을 저장할지"만 표현하고,
 * "어디에 저장할지"는 포함하지 않습니다.
 */
data class FixtureResponseCacheDocument(
    val snapshotJson: String,
    val etagHash: String,
)
