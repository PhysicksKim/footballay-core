package com.footballay.core.web.admin.localization.ai

import com.fasterxml.jackson.databind.JsonNode
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.facade.LeagueFacade
import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.admin.localization.dto.AiLocalizationImportValidationError
import com.footballay.core.web.admin.localization.dto.AiLocalizationImportValidationFailureResponse
import org.springframework.stereotype.Component

/** AI import payload의 형식과 Core UID를 검증합니다. */
@Component
class AiLocalizationImportValidator(
    private val leagueFacade: LeagueFacade,
) {
    fun validate(payload: JsonNode): AiLocalizationImportValidationResult {
        val errors = mutableListOf<AiLocalizationImportValidationError>()
        if (!payload.isObject) {
            errors += AiLocalizationImportValidationError("INVALID_FIELD_TYPE", "payload는 object여야 합니다.", field = "payload")
            return failure(errors)
        }

        val version = payload.requiredInt("version", errors)
        if (version != null && version != AiLocalizationContract.VERSION) {
            errors += AiLocalizationImportValidationError("UNSUPPORTED_VERSION", "지원하지 않는 version입니다: $version", field = "version")
        }

        val entityType = readEntityType(payload, errors)
        val itemsNode = payload.requiredArray("items", errors) ?: return failure(errors)
        if (itemsNode.isEmpty) errors += AiLocalizationImportValidationError("ITEMS_EMPTY", "items는 비어 있을 수 없습니다.", field = "items")

        val items = readItems(itemsNode, errors)
        validateCoreUids(entityType, items, errors)

        if (errors.isNotEmpty()) return failure(errors)
        return AiLocalizationImportValidationResult.success(ValidatedAiLocalizationImport(requireNotNull(entityType), items))
    }

    private fun readEntityType(
        payload: JsonNode,
        errors: MutableList<AiLocalizationImportValidationError>,
    ): AiLocalizationEntityType? {
        val value = payload.requiredText("entityType", errors) ?: return null
        return AiLocalizationEntityType.entries.find { it.name == value } ?: run {
            errors += AiLocalizationImportValidationError("UNSUPPORTED_ENTITY_TYPE", "지원하지 않는 entityType입니다: $value", field = "entityType")
            null
        }
    }

    private fun readItems(
        itemsNode: JsonNode,
        errors: MutableList<AiLocalizationImportValidationError>,
    ): List<ValidatedAiLocalizationImportItem> {
        val result = mutableListOf<ValidatedAiLocalizationImportItem>()
        val seen = mutableSetOf<Pair<String, String>>()
        itemsNode.forEachIndexed { index, item ->
            if (!item.isObject) {
                errors += AiLocalizationImportValidationError("INVALID_FIELD_TYPE", "item은 object여야 합니다.", index = index, field = "items[$index]")
                return@forEachIndexed
            }

            val uid = item.requiredText("uid", errors, index)
            if (uid != null && uid.isBlank()) {
                errors += AiLocalizationImportValidationError("UID_BLANK", "uid는 blank일 수 없습니다.", index = index, uid = uid, field = "items[$index].uid")
            }

            val localeCode = item.requiredText("locale", errors, index, uid)
            validateDuplicate(uid, localeCode, index, seen, errors)
            val locale = readLocale(localeCode, uid, index, errors)
            val name = item.optionalText("name", errors, index, uid, localeCode)
            val shortName = item.optionalText("shortName", errors, index, uid, localeCode)
            validateLength(name, "name", "NAME_TOO_LONG", uid, localeCode, index, errors)
            validateLength(shortName, "shortName", "SHORT_NAME_TOO_LONG", uid, localeCode, index, errors)
            if (uid != null && uid.isNotBlank() && locale != null) result += ValidatedAiLocalizationImportItem(index, uid, locale, name, shortName)
        }
        return result
    }

    private fun validateDuplicate(
        uid: String?,
        localeCode: String?,
        index: Int,
        seen: MutableSet<Pair<String, String>>,
        errors: MutableList<AiLocalizationImportValidationError>,
    ) {
        if (uid == null || uid.isBlank() || localeCode == null) return
        if (!seen.add(uid to localeCode)) errors += AiLocalizationImportValidationError("DUPLICATE_UID_LOCALE", "(uid, locale) 조합이 중복되었습니다.", index, uid, localeCode)
    }

    private fun readLocale(
        localeCode: String?,
        uid: String?,
        index: Int,
        errors: MutableList<AiLocalizationImportValidationError>,
    ): SupportedLocale? {
        if (localeCode == null) return null
        val locale = SupportedLocale.entries.find { it.code == localeCode }
        if (locale == null) errors += AiLocalizationImportValidationError("UNSUPPORTED_LOCALE", "지원하지 않는 locale입니다: $localeCode", index, uid, localeCode, "items[$index].locale")
        return locale
    }

    private fun validateLength(
        value: String?,
        field: String,
        code: String,
        uid: String?,
        locale: String?,
        index: Int,
        errors: MutableList<AiLocalizationImportValidationError>,
    ) {
        if (value != null && value.length > 255) errors += AiLocalizationImportValidationError(code, "${field}은 255자를 초과할 수 없습니다.", index, uid, locale, "items[$index].$field")
    }

    private fun validateCoreUids(
        entityType: AiLocalizationEntityType?,
        items: List<ValidatedAiLocalizationImportItem>,
        errors: MutableList<AiLocalizationImportValidationError>,
    ) {
        if (entityType == null) return
        val existingByUid =
            items.map { it.uid }.distinct().associateWith { uid ->
                when (entityType) {
                    AiLocalizationEntityType.TEAM -> leagueFacade.findTeamByUid(uid) is DomainResult.Success
                    AiLocalizationEntityType.PLAYER -> leagueFacade.findPlayerByUid(uid) is DomainResult.Success
                }
            }
        val coreType = if (entityType == AiLocalizationEntityType.TEAM) "TeamCore" else "PlayerCore"
        items.filter { existingByUid[it.uid] == false }.forEach { item ->
            errors += AiLocalizationImportValidationError("CORE_NOT_FOUND", "$coreType not found.", item.index, item.uid, item.locale.code)
        }
    }

    private fun failure(errors: List<AiLocalizationImportValidationError>)
        = AiLocalizationImportValidationResult.failure(AiLocalizationImportValidationFailureResponse(errors))
}
