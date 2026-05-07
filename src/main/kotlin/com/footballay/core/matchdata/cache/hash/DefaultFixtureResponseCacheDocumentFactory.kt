package com.footballay.core.matchdata.cache.hash

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.web.football.dto.FixtureEventsResponse
import com.footballay.core.web.football.dto.FixtureLineupResponse
import com.footballay.core.web.football.dto.FixtureLiveStatusResponse
import com.footballay.core.web.football.dto.FixtureStatisticsResponse
import org.springframework.stereotype.Component

/**
 * [FixtureResponseCacheDocumentFactory]의 기본 구현입니다.
 *
 * Fixture 응답을 Etag Cache하기 위해 필요한 인터페이스들을 호출하고, 캐싱에 필요한 산출물들을 반환합니다.
 *
 * 조합 흐름:
 *
 * 1. 원본 DTO를 `ObjectMapper`로 직렬화해 `snapshotJson` 생성
 * 2. 같은 DTO를 `FixtureResponseCanonicalizer`에 전달해 canonical DTO 생성
 * 3. canonical DTO를 `FixtureCanonicalJsonWriter`로 직렬화해 canonical JSON bytes 생성
 * 4. canonical JSON bytes를 `FixtureEtagHasher`에 전달해 `etagHash` 생성
 *
 * 즉 이 구현체의 역할은 "응답 표현물 생성 orchestration"이지,
 * 정렬 규칙 정의, canonical JSON 규약 정의, hash 규칙 정의 자체가 아닙니다.
 *
 * 또한 이 구현체는 cache key, TTL, 저장소 종류를 알지 않습니다.
 * HTTP `ETag` 헤더 포맷(`W/"..."`) 역시 다루지 않습니다.
 * 생성된 [FixtureResponseCacheDocument]를 어디에 저장할지는 상위 계층이 결정합니다.
 */
@Component
class DefaultFixtureResponseCacheDocumentFactory(
    private val objectMapper: ObjectMapper,
    private val canonicalizer: FixtureResponseCanonicalizer,
    private val canonicalJsonWriter: FixtureCanonicalJsonWriter,
    private val etagHasher: FixtureEtagHasher,
) : FixtureResponseCacheDocumentFactory {
    override fun create(response: FixtureLiveStatusResponse): FixtureResponseCacheDocument = create(response, canonicalizer.canonicalize(response))

    override fun create(response: FixtureEventsResponse): FixtureResponseCacheDocument = create(response, canonicalizer.canonicalize(response))

    override fun create(response: FixtureLineupResponse): FixtureResponseCacheDocument = create(response, canonicalizer.canonicalize(response))

    override fun create(response: FixtureStatisticsResponse): FixtureResponseCacheDocument = create(response, canonicalizer.canonicalize(response))

    private fun create(
        response: Any,
        canonicalResponse: Any,
    ): FixtureResponseCacheDocument {
        val snapshotJson = objectMapper.writeValueAsString(response)
        val etagHash = etagHasher.hash(canonicalJsonWriter.writeAsBytes(canonicalResponse))

        return FixtureResponseCacheDocument(
            snapshotJson = snapshotJson,
            etagHash = etagHash,
        )
    }
}
