package com.footballay.core.matchdata.cache.hash

/**
 * HTTP `ETag` / `If-None-Match` 헤더 표현을 다룹니다.
 *
 * 내부 저장/비교 기준인 `etagHash`와
 * HTTP wire format(`W/"..."`)을 분리하기 위한 helper 입니다.
 */
interface FixtureHttpEtagHelper {
    fun toWeakEtag(etagHash: String): String

    fun matchesIfNoneMatch(
        ifNoneMatchHeader: String?,
        etagHash: String,
    ): Boolean
}
