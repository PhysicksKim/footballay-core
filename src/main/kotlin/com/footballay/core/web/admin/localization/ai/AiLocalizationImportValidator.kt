package com.footballay.core.web.admin.localization.ai

import com.fasterxml.jackson.databind.JsonNode
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.facade.LeagueFacade
import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.admin.localization.dto.AiLocalizationImportEntityType
import com.footballay.core.web.admin.localization.dto.AiLocalizationImport
import com.footballay.core.web.admin.localization.dto.AiLocalizationImportItem
import com.footballay.core.web.admin.localization.dto.AiLocalizationImportValidationError
import com.footballay.core.web.admin.localization.dto.AiLocalizationImportValidationFailureResponse
import com.footballay.core.web.admin.localization.dto.AiLocalizationImportValidationResult
import org.springframework.stereotype.Component

/** Admin AI localization import payload의 contract와 Core UID를 검증합니다. */
@Component
class AiLocalizationImportValidator(
    private val leagueFacade: LeagueFacade,
) {
    fun validate(payload: JsonNode): AiLocalizationImportValidationResult {
        val errors = mutableListOf<AiLocalizationImportValidationError>()
        val request = readPayloadObject(payload, errors) ?: return failureResult(errors)

        val version = readVersion(request, errors)
        checkSupportedVersion(version, errors)

        val entityType = readEntityType(request, errors)
        val items = readItems(request, errors) ?: return failureResult(errors)
        checkItemsNotEmpty(items, errors)

        val validatedItems = validateItems(items, errors)
        checkCoreUids(entityType, validatedItems, errors)

        return validationResult(entityType, validatedItems, errors)
    }

    private fun readPayloadObject(
        payload: JsonNode,
        errors: MutableList<AiLocalizationImportValidationError>,
    ): JsonNode? {
        if (payload.isObject) return payload
        errors += AiLocalizationImportValidationError("INVALID_FIELD_TYPE", "payload는 object여야 합니다.", field = "payload")
        return null
    }

    private fun readVersion(
        payload: JsonNode,
        errors: MutableList<AiLocalizationImportValidationError>,
    ): Int? = payload.requiredIntegral("version", errors)

    private fun checkSupportedVersion(
        version: Int?,
        errors: MutableList<AiLocalizationImportValidationError>,
    ) {
        if (version != null && version != AiLocalizationContract.VERSION) {
            errors += AiLocalizationImportValidationError("UNSUPPORTED_VERSION", "지원하지 않는 version입니다: $version", field = "version")
        }
    }

    private fun readEntityType(
        payload: JsonNode,
        errors: MutableList<AiLocalizationImportValidationError>,
    ): AiLocalizationImportEntityType? {
        val value = payload.requiredText("entityType", errors) ?: return null
        return AiLocalizationImportEntityType.entries.find { it.name == value }
            ?: run {
                errors += AiLocalizationImportValidationError("UNSUPPORTED_ENTITY_TYPE", "지원하지 않는 entityType입니다: $value", field = "entityType")
                null
            }
    }

    private fun readItems(
        payload: JsonNode,
        errors: MutableList<AiLocalizationImportValidationError>,
    ): JsonNode? {
        val items = payload.get("items")
        if (items == null) {
            errors += AiLocalizationImportValidationError("MISSING_REQUIRED_FIELD", "items는 필수입니다.", field = "items")
            return null
        }
        if (items.isArray) return items
        errors += AiLocalizationImportValidationError("INVALID_FIELD_TYPE", "items는 array여야 합니다.", field = "items")
        return null
    }

    private fun checkItemsNotEmpty(
        items: JsonNode,
        errors: MutableList<AiLocalizationImportValidationError>,
    ) {
        if (items.isEmpty) errors += AiLocalizationImportValidationError("ITEMS_EMPTY", "items는 비어 있을 수 없습니다.", field = "items")
    }

    private fun validateItems(
        items: JsonNode,
        errors: MutableList<AiLocalizationImportValidationError>,
    ): List<AiLocalizationImportItem> {
        val validatedItems = mutableListOf<AiLocalizationImportItem>()
        val seen = mutableSetOf<Pair<String, String>>()
        items.forEachIndexed { index, item ->
            if (!item.isObject) {
                errors += AiLocalizationImportValidationError("INVALID_FIELD_TYPE", "item은 object여야 합니다.", index = index, field = "items[$index]")
                return@forEachIndexed
            }
            val uid = item.requiredText("uid", errors, index)
            checkUidNotBlank(uid, index, errors)
            val localeCode = item.requiredText("locale", errors, index, uid)
            checkDuplicateUidLocale(uid, localeCode, index, seen, errors)
            val locale = readSupportedLocale(localeCode, index, uid, errors)
            val name = item.optionalText("name", errors, index, uid, localeCode)
            checkMaximumLength(name, "name", "NAME_TOO_LONG", index, uid, localeCode, errors)
            val shortName = item.optionalText("shortName", errors, index, uid, localeCode)
            checkMaximumLength(shortName, "shortName", "SHORT_NAME_TOO_LONG", index, uid, localeCode, errors)
            if (uid != null && !uid.isBlank() && locale != null) {
                validatedItems += AiLocalizationImportItem(index, uid, locale, name, shortName)
            }
        }
        return validatedItems
    }

    private fun checkUidNotBlank(
        uid: String?,
        index: Int,
        errors: MutableList<AiLocalizationImportValidationError>,
    ) {
        if (uid != null && uid.isBlank()) {
            errors += AiLocalizationImportValidationError("UID_BLANK", "uid는 blank일 수 없습니다.", index = index, uid = uid, field = "items[$index].uid")
        }
    }

    private fun checkDuplicateUidLocale(
        uid: String?,
        locale: String?,
        index: Int,
        seen: MutableSet<Pair<String, String>>,
        errors: MutableList<AiLocalizationImportValidationError>,
    ) {
        if (uid != null && !uid.isBlank() && locale != null && !seen.add(uid to locale)) {
            errors += AiLocalizationImportValidationError("DUPLICATE_UID_LOCALE", "(uid, locale) 조합이 중복되었습니다.", index, uid, locale)
        }
    }

    private fun readSupportedLocale(
        localeCode: String?,
        index: Int,
        uid: String?,
        errors: MutableList<AiLocalizationImportValidationError>,
    ): SupportedLocale? {
        val locale = localeCode?.toSupportedLocale()
        if (localeCode != null && locale == null) {
            errors += AiLocalizationImportValidationError("UNSUPPORTED_LOCALE", "지원하지 않는 locale입니다: $localeCode", index, uid, localeCode, "items[$index].locale")
        }
        return locale
    }

    private fun checkMaximumLength(
        value: String?,
        field: String,
        code: String,
        index: Int,
        uid: String?,
        locale: String?,
        errors: MutableList<AiLocalizationImportValidationError>,
    ) {
        if (value != null && value.length > 255) {
            errors += AiLocalizationImportValidationError(code, "${field}은 255자를 초과할 수 없습니다.", index, uid, locale, "items[$index].$field")
        }
    }

    private fun checkCoreUids(
        entityType: AiLocalizationImportEntityType?,
        items: List<AiLocalizationImportItem>,
        errors: MutableList<AiLocalizationImportValidationError>,
    ) {
        if (entityType == null) return
        val existingUids = items.map { it.uid }.distinct().associateWith { uid ->
            when (entityType) {
                AiLocalizationImportEntityType.TEAM -> leagueFacade.findTeamByUid(uid) is DomainResult.Success
                AiLocalizationImportEntityType.PLAYER -> leagueFacade.findPlayerByUid(uid) is DomainResult.Success
            }
        }
        items.filter { existingUids[it.uid] == false }.forEach { item ->
            errors += AiLocalizationImportValidationError(
                "CORE_NOT_FOUND",
                "${entityType.name.lowercase().replaceFirstChar(Char::uppercase)}Core not found.",
                item.index,
                item.uid,
                item.locale.code,
            )
        }
    }

    private fun JsonNode.requiredText(
        field: String,
        errors: MutableList<AiLocalizationImportValidationError>,
        index: Int? = null,
        uid: String? = null,
    ): String? {
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

    private fun JsonNode.requiredIntegral(
        field: String,
        errors: MutableList<AiLocalizationImportValidationError>,
    ): Int? {
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

    private fun JsonNode.optionalText(
        field: String,
        errors: MutableList<AiLocalizationImportValidationError>,
        index: Int,
        uid: String?,
        locale: String?,
    ): String? {
        val value = get(field) ?: return null
        if (value.isNull) return null
        if (!value.isTextual) {
            errors += AiLocalizationImportValidationError("INVALID_FIELD_TYPE", "${field}는 null 또는 string이어야 합니다.", index, uid, locale, "items[$index].$field")
            return null
        }
        return value.textValue()
    }

    private fun String.toSupportedLocale(): SupportedLocale? = SupportedLocale.entries.find { it.code == this }

    private fun fieldPath(index: Int?, field: String): String = if (index == null) field else "items[$index].$field"

    private fun failureResult(
        errors: List<AiLocalizationImportValidationError>,
    ): AiLocalizationImportValidationResult = AiLocalizationImportValidationResult.failure(AiLocalizationImportValidationFailureResponse(errors))

    private fun validationResult(
        entityType: AiLocalizationImportEntityType?,
        items: List<AiLocalizationImportItem>,
        errors: List<AiLocalizationImportValidationError>,
    ): AiLocalizationImportValidationResult =
        errors.takeIf { it.isNotEmpty() }?.let(::failureResult)
            ?: AiLocalizationImportValidationResult.success(AiLocalizationImport(requireNotNull(entityType), items))
}
