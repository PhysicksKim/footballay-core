package com.footballay.core.localization

data class CoreLocalizationModel(
    val coreUid: String,
    val locale: SupportedLocale,
    val name: String?,
    val shortName: String?,
    val aiGenerated: Boolean,
)

data class LocalizationUpsertResult(
    val localization: CoreLocalizationModel?,
)
