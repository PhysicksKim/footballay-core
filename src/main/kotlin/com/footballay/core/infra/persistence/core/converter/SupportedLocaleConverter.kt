package com.footballay.core.infra.persistence.core.converter

import com.footballay.core.localization.SupportedLocale
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/** [SupportedLocale]을 lowercase DB 값으로 변환합니다. */
@Converter(autoApply = true)
class SupportedLocaleConverter : AttributeConverter<SupportedLocale, String> {
    override fun convertToDatabaseColumn(attribute: SupportedLocale): String = attribute.code

    override fun convertToEntityAttribute(dbData: String): SupportedLocale =
        SupportedLocale.entries.firstOrNull { it.code == dbData }
            ?: throw IllegalArgumentException("Unsupported locale code: $dbData")
}
