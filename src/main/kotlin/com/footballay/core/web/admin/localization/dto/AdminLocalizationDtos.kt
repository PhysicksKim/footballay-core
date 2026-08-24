package com.footballay.core.web.admin.localization.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.footballay.core.web.admin.localization.ai.AiLocalizationContract
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.NotEmpty

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

enum class AiLocalizationExportEntityType {
    TEAM,
    PLAYER,
}

enum class AiLocalizationImportEntityType {
    TEAM,
    PLAYER,
}

data class AiLocalizationExportRequest(
    val entityType: AiLocalizationExportEntityType,
    val leagueUid: String,
    val teamUid: String? = null,
    @field:NotEmpty
    val locales: List<String>,
    @field:NotEmpty
    val uids: List<String>,
)

data class AiLocalizationExportResponse(
    val version: Int = AiLocalizationContract.VERSION,
    val locales: List<String>,
    val entityType: AiLocalizationExportEntityType,
    val context: AiLocalizationExportContext,
    val items: List<AiLocalizationExportItem>,
)

data class AiLocalizationExportContext(
    val league: AiLocalizationExportContextItem,
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    val team: AiLocalizationExportContextItem? = null,
)

data class AiLocalizationExportContextItem(
    val uid: String,
    val originalName: String,
)

data class AiLocalizationExportItem(
    val uid: String,
    val originalName: String,
    val localizations: Map<String, AiLocalizationExportValue>,
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class AiLocalizationExportValue(
    val name: String?,
    val shortName: String?,
)
