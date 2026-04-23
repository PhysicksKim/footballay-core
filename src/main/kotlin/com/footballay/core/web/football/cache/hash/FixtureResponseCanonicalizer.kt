package com.footballay.core.web.football.cache.hash

import com.footballay.core.web.football.dto.FixtureEventsResponse
import com.footballay.core.web.football.dto.FixtureLineupResponse
import com.footballay.core.web.football.dto.FixtureLiveStatusResponse
import com.footballay.core.web.football.dto.FixtureStatisticsResponse

/**
 * 해시 계산 전에 응답 DTO를 정규화합니다.
 *
 * 이 타입의 책임은 "해시 입력 안정화"입니다.
 * 즉, 외부에서 어떤 순서로 DTO가 들어오더라도 동일한 semantic 데이터라면
 * 동일한 canonical 결과로 수렴시키는 역할만 담당합니다.
 *
 * 현재 fixture 응답의 배열 순서는 API contract 로 보장하지 않으므로,
 * canonicalizer 가 정렬한 결과가 이후 응답 직렬화에 재사용되더라도
 * 그것만으로는 contract 변경으로 보지 않습니다.
 *
 * 반대로, canonicalizer 는 응답 표현 정책 자체를 정의하지 않습니다.
 * ETag 안정성에 직접 영향을 주는 JSON bytes 규약은 별도의 canonical JSON writer 가 책임집니다.
 */
interface FixtureResponseCanonicalizer {
    fun canonicalize(response: FixtureLiveStatusResponse): FixtureLiveStatusResponse

    fun canonicalize(response: FixtureEventsResponse): FixtureEventsResponse

    fun canonicalize(response: FixtureLineupResponse): FixtureLineupResponse

    fun canonicalize(response: FixtureStatisticsResponse): FixtureStatisticsResponse
}
