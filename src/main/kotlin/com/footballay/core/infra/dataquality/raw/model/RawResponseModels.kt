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
    val contentType: String = "application/gzip",
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawResponseUploadCommand

        if (rawJsonObjectKey != other.rawJsonObjectKey) return false
        if (!gzipBytes.contentEquals(other.gzipBytes)) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rawJsonObjectKey.hashCode()
        result = 31 * result + gzipBytes.contentHashCode()
        result = 31 * result + contentType.hashCode()
        return result
    }
}

data class RawResponseStoredObject(
    val rawJsonObjectKey: String,
)

data class RawResponseDownloadUrlCommand(
    val rawJsonObjectKey: String,
)

data class RawResponseDownloadUrl(
    val downloadUrl: String,
    val expiresAt: Instant,
)

data class RawResponseCollectedEvent(
    val rawEventId: String,
    val provider: FootballDataProvider,
    val endpointKey: String,
    val parameters: List<RawResponseParameter>,
    val canonicalHash: String,
    val rawJsonObjectKey: String,
    val collectedAt: Instant,
)
