package com.footballay.core.web.admin.localization.ai

import com.fasterxml.jackson.databind.JsonNode
import com.footballay.core.web.admin.localization.dto.AiLocalizationImportValidationError

internal fun JsonNode.requiredText(field: String, errors: MutableList<AiLocalizationImportValidationError>, index: Int? = null, uid: String? = null): String? {
    val value = get(field)
    if (value == null) {
        errors += AiLocalizationImportValidationError("MISSING_REQUIRED_FIELD", "${field}는 필수입니다.", index, uid, field = fieldPath(index, field))
        return null
    }
    if (!value.isTextual) {
        errors += AiLocalizationImportValidationError("INVALID_FIELD_TYPE", "${field}는 string이어야 합니다.", index, uid, field = fieldPath(index, field))
        return null
    }
    return value.textValue()
}

internal fun JsonNode.optionalText(field: String, errors: MutableList<AiLocalizationImportValidationError>, index: Int, uid: String?, locale: String?): String? {
    val value = get(field) ?: return null
    if (value.isNull) return null
    if (!value.isTextual) {
        errors += AiLocalizationImportValidationError("INVALID_FIELD_TYPE", "${field}는 null 또는 string이어야 합니다.", index, uid, locale, "items[$index].$field")
        return null
    }
    return value.textValue()
}

internal fun JsonNode.requiredInt(field: String, errors: MutableList<AiLocalizationImportValidationError>): Int? {
    val value = get(field)
    if (value == null) {
        errors += AiLocalizationImportValidationError("MISSING_REQUIRED_FIELD", "${field}는 필수입니다.", field = field)
        return null
    }
    if (!value.isIntegralNumber) {
        errors += AiLocalizationImportValidationError("INVALID_FIELD_TYPE", "${field}는 integer여야 합니다.", field = field)
        return null
    }
    return value.intValue()
}

internal fun JsonNode.requiredArray(field: String, errors: MutableList<AiLocalizationImportValidationError>): JsonNode? {
    val value = get(field)
    if (value == null) {
        errors += AiLocalizationImportValidationError("MISSING_REQUIRED_FIELD", "${field}는 필수입니다.", field = field)
        return null
    }
    if (!value.isArray) {
        errors += AiLocalizationImportValidationError("INVALID_FIELD_TYPE", "${field}는 array여야 합니다.", field = field)
        return null
    }
    return value
}

private fun fieldPath(index: Int?, field: String) = if (index == null) field else "items[$index].$field"
