package com.footballay.core.web.admin.localization.dto

import jakarta.validation.constraints.Size

data class SupportedLocaleResponse(
    val code: String,
)

data class LocalizationResponse(
    val name: String?,
    val shortName: String?,
    val aiGenerated: Boolean,
)

data class CoreLocalizationResponse(
    val uid: String,
    val originalName: String,
    val localization: LocalizationResponse?,
)

data class LocalizationUpdateRequest(
    @field:Size(max = 255)
    val name: String? = null,
    @field:Size(max = 255)
    val shortName: String? = null,
)
