package com.footballay.core.web.admin.localization.ai

import com.footballay.core.localization.SupportedLocale

/** 검증된 AI import 항목을 apply 단계로 전달합니다. */
data class ValidatedAiLocalizationImport(
    val entityType: AiLocalizationEntityType,
    val items: List<ValidatedAiLocalizationImportItem>,
)

data class ValidatedAiLocalizationImportItem(
    val index: Int,
    val uid: String,
    val locale: SupportedLocale,
    val name: String?,
    val shortName: String?,
)
