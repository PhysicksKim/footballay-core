package com.footballay.core.web.football.cache.hash

/**
 * canonical JSON bytes로부터 캐시 검증용 ETag 값을 계산합니다.
 */
interface FixtureEtagHasher {
    /**
     * canonical JSON bytes를 SHA-256 후 base64url(no padding) 문자열로 반환합니다.
     */
    fun hash(bytes: ByteArray): String

    /**
     * 내부 hash 문자열을 weak ETag 헤더 값으로 감쌉니다.
     */
    fun toWeakEtag(hash: String): String

    /**
     * canonical JSON bytes를 바로 weak ETag 헤더 값으로 변환합니다.
     */
    fun hashToWeakEtag(bytes: ByteArray): String
}
