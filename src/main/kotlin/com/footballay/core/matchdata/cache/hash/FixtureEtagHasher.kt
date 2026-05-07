package com.footballay.core.matchdata.cache.hash

/**
 * canonical JSON bytes로부터 캐시 검증용 hash 값을 계산합니다.
 */
interface FixtureEtagHasher {
    /**
     * canonical JSON bytes를 SHA-256 후 base64url(no padding) 문자열로 반환합니다.
     */
    fun hash(bytes: ByteArray): String
}
