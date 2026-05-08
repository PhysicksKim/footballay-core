package com.footballay.core.cache.matchdata.polling.hash

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.stereotype.Component

/**
 * 해시 입력 안정성을 위해 전용 Jackson writer 설정을 고정합니다.
 *
 * - compact JSON
 * - null 포함
 * - property order 고정
 * - map key order 고정
 */
@Component
class DefaultFixtureCanonicalJsonWriter(
    objectMapper: ObjectMapper,
) : FixtureCanonicalJsonWriter {
    @Suppress("DEPRECATION")
    private val canonicalObjectWriter: ObjectWriter =
        objectMapper
            .copy()
            .apply {
                setSerializationInclusion(JsonInclude.Include.ALWAYS)
                configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                configure(SerializationFeature.INDENT_OUTPUT, false)
            }.writer()

    override fun writeAsBytes(response: Any): ByteArray = canonicalObjectWriter.writeValueAsBytes(response)

    override fun writeAsString(response: Any): String = canonicalObjectWriter.writeValueAsString(response)
}
