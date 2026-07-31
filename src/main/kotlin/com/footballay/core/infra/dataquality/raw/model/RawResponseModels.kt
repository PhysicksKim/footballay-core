package com.footballay.core.infra.dataquality.raw.model

import java.time.Instant

enum class FootballDataProvider {
    API_SPORTS,
    // SPORTMONKS, // future plan 추가할 provider 후보
}

data class RawResponseCollectionCommand(
    val provider: FootballDataProvider,
    val endpointKey: String,
    val parameters: List<RawResponseParameter>,
    val rawJson: String,
    val collectedAt: Instant,
)

data class RawResponseParameter(
    val name: String,
    val value: String,
)

data class RawResponseDuplicateCheckCommand(
    val provider: FootballDataProvider,
    val endpointKey: String,
    val parameters: List<RawResponseParameter>,
    val canonicalHash: String,
)

data class RawResponseObjectKeyCommand(
    val provider: FootballDataProvider,
    val endpointKey: String,
    val parameters: List<RawResponseParameter>,
    val collectedAt: Instant,
    val canonicalHash: String,
)

sealed interface RawResponseDuplicateCheckResult {
    data object New : RawResponseDuplicateCheckResult

    data object Duplicate : RawResponseDuplicateCheckResult

    data class Failed(
        val reason: String,
    ) : RawResponseDuplicateCheckResult
}

data class RawResponseUploadCommand(
    val rawJsonObjectKey: String,
    val gzipBytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawResponseUploadCommand

        if (rawJsonObjectKey != other.rawJsonObjectKey) return false
        if (!gzipBytes.contentEquals(other.gzipBytes)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = rawJsonObjectKey.hashCode()
        result = 31 * result + gzipBytes.contentHashCode()
        return result
    }
}

data class RawResponseStoredObject(
    val rawJsonObjectKey: String,
)

/**
 * Raw response 저장 후 Data Quality 처리를 요청하는 이벤트입니다.
 *
 * @property rawEventId 원본 응답 수집 건을 식별하는 ULID입니다.
 * @property provider 원본 응답을 제공한 축구 데이터 제공자입니다.
 * @property endpointKey 수집한 제공자 API endpoint 식별자입니다.
 * @property parameters 요청을 구분하는 endpoint 파라미터입니다.
 * @property canonicalHash 중복 감지에 사용하는 정규화된 원본 응답의 해시입니다.
 * @property rawJsonObjectKey 저장소 내부에서 원본 gzip 파일을 식별하는 안정적인 object key입니다.
 * @property collectedAt 원본 응답을 수집한 시각입니다.
 */
data class RawResponseCollectedEvent(
    val rawEventId: String,
    val provider: FootballDataProvider,
    val endpointKey: String,
    val parameters: List<RawResponseParameter>,
    val canonicalHash: String,
    /**
     * 현재 배포 환경에 설정된 primary raw-response storage 내부의 object key입니다.
     *
     * Consumer는 자신의 storage 설정을 사용해 이 key로 원본을 조회합니다.
     * bucket, endpoint, credential, presigned URL은 이벤트 계약에 포함하지 않습니다.
     */
    val rawJsonObjectKey: String,
    val collectedAt: Instant,
)
