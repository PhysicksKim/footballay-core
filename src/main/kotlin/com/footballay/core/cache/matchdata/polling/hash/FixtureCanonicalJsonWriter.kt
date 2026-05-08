package com.footballay.core.cache.matchdata.polling.hash

/**
 * canonicalized fixture DTO를 해시 계산용 canonical JSON으로 직렬화합니다.
 *
 * 이 타입은 배열 순서 정규화는 담당하지 않습니다.
 * 입력 DTO는 이미 canonicalizer를 거쳐 안정적인 리스트 순서를 가져야 합니다.
 */
interface FixtureCanonicalJsonWriter {
    fun writeAsBytes(response: Any): ByteArray

    fun writeAsString(response: Any): String
}
