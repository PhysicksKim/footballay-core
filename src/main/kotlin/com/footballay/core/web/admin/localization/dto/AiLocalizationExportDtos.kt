package com.footballay.core.web.admin.localization.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.footballay.core.web.admin.localization.ai.AiLocalizationContract
import com.footballay.core.web.admin.localization.ai.AiLocalizationEntityType
import jakarta.validation.constraints.NotEmpty

data class AiLocalizationExportRequest(
    val entityType: AiLocalizationEntityType,
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
    val entityType: AiLocalizationEntityType,
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
