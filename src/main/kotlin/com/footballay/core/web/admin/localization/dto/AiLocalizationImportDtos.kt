package com.footballay.core.web.admin.localization.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.footballay.core.localization.SupportedLocale

data class AiLocalizationImportItem(
    val index: Int,
    val uid: String,
    val locale: SupportedLocale,
    val name: String?,
    val shortName: String?,
)

data class AiLocalizationImport(
    val entityType: AiLocalizationImportEntityType,
    val items: List<AiLocalizationImportItem>,
)

class AiLocalizationImportValidationResult private constructor(
    val value: AiLocalizationImport?,
    val failure: AiLocalizationImportValidationFailureResponse?,
) {
    val isSuccess: Boolean
        get() = value != null

    companion object {
        fun success(value: AiLocalizationImport): AiLocalizationImportValidationResult = AiLocalizationImportValidationResult(value, null)

        fun failure(failure: AiLocalizationImportValidationFailureResponse): AiLocalizationImportValidationResult =
            AiLocalizationImportValidationResult(null, failure)
    }
}

data class AiLocalizationImportValidationFailureResponse(
    val errors: List<AiLocalizationImportValidationError>,
)

data class AiLocalizationImportResponse(
    val updatedCount: Int,
    val unchangedCount: Int,
    val changes: List<AiLocalizationImportChangeResponse>,
)

data class AiLocalizationImportChangeResponse(
    val uid: String,
    val locale: String,
    val before: AiLocalizationImportValueResponse?,
    val after: AiLocalizationImportValueResponse,
)

data class AiLocalizationImportValueResponse(
    val name: String?,
    val shortName: String?,
    val aiGenerated: Boolean,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AiLocalizationImportValidationError(
    val code: String,
    val message: String,
    val index: Int? = null,
    val uid: String? = null,
    val locale: String? = null,
    val field: String? = null,
)
