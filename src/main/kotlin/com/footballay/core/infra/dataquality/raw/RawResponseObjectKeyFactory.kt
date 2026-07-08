package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.config.DataQualityProperties
import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import com.footballay.core.infra.dataquality.raw.model.RawResponseObjectKeyCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseParameter
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Builds the persistent object key for a compressed raw football data response.
 */
interface RawResponseObjectKeyFactory {
    /**
     * Creates an S3-compatible object key, not a URL or filesystem path.
     */
    fun create(command: RawResponseObjectKeyCommand): String
}

/**
 * Default key layout:
 * data-quality/raw/{provider}/{endpoint}/{yyyy}/{MM}/{dd}/{parameters}/{timestamp}_{hash}.json.gz
 */
@Component
class DefaultRawResponseObjectKeyFactory(
    properties: DataQualityProperties,
) : RawResponseObjectKeyFactory {
    private val rawPrefix: String = normalizeRawPrefix(properties.storage.rawPrefix)

    override fun create(command: RawResponseObjectKeyCommand): String {
        val providerPath = providerPath(command.provider)
        val endpointKey = validatePathSegment("endpointKey", command.endpointKey)
        val parameterPath = parameterPath(command.parameters)
        val canonicalHash = validatePathSegment("canonicalHash", command.canonicalHash)
        val collectedAt = command.collectedAt.atOffset(ZoneOffset.UTC)
        val datePartitionPath = DATE_PARTITION_PATH_FORMATTER.format(collectedAt)
        val timestamp = TIMESTAMP_FORMATTER.format(collectedAt)

        return "$rawPrefix/$providerPath/$endpointKey/$datePartitionPath/$parameterPath/${timestamp}_$canonicalHash.json.gz"
    }

    private fun providerPath(provider: FootballDataProvider): String =
        provider.name
            .lowercase(Locale.ROOT)
            .replace('_', '-')

    private fun validatePathSegment(
        name: String,
        value: String,
    ): String {
        val trimmed = value.trim()
        require(trimmed.isNotBlank()) {
            "$name must not be blank"
        }
        require(!trimmed.contains('/') && !trimmed.contains('\\')) {
            "$name must not contain path separators"
        }
        require(!trimmed.contains("://") && !trimmed.contains('?') && !trimmed.contains('&')) {
            "$name must not contain URL or query string characters"
        }
        return trimmed
    }

    private fun parameterPath(parameters: List<RawResponseParameter>): String {
        require(parameters.isNotEmpty()) {
            "parameters must not be empty"
        }
        return parameters.joinToString("_") { parameter ->
            val name = validatePathSegment("parameter name", parameter.name)
            val value = validatePathSegment("parameter value", parameter.value)
            "$name-$value"
        }
    }

    private fun normalizeRawPrefix(rawPrefix: String): String {
        val normalized = rawPrefix.trim().trim('/')
        require(normalized.isNotBlank()) {
            "rawPrefix must not be blank"
        }
        return normalized
    }

    private companion object {
        /**
         * 날짜 분류에 따른 의도적인 경로 추가 생성을 위해 "년/월/일" 형태로 구성합니다.
         */
        val DATE_PARTITION_PATH_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        val TIMESTAMP_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
    }
}
