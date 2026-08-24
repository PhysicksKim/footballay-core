package com.footballay.core.localization

/** AI import가 localization 계층에 전달하는 검증 완료 변경값입니다. */
data class AiLocalizationUpdate(
    val coreUid: String,
    val locale: SupportedLocale,
    val name: String?,
    val shortName: String?,
)

data class AiLocalizationApplyChange(
    val coreUid: String,
    val locale: SupportedLocale,
    val before: CoreLocalizationModel?,
    val after: CoreLocalizationModel,
)

data class AiLocalizationApplyResult(
    val updatedCount: Int,
    val unchangedCount: Int,
    val changes: List<AiLocalizationApplyChange>,
)
