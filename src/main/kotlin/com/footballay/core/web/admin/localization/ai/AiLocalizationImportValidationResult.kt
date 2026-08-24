package com.footballay.core.web.admin.localization.ai

import com.footballay.core.web.admin.localization.dto.AiLocalizationImportValidationFailureResponse

class AiLocalizationImportValidationResult private constructor(
    val value: ValidatedAiLocalizationImport?,
    val failure: AiLocalizationImportValidationFailureResponse?,
) {
    val isSuccess: Boolean
        get() = value != null

    companion object {
        fun success(value: ValidatedAiLocalizationImport) = AiLocalizationImportValidationResult(value, null)

        fun failure(failure: AiLocalizationImportValidationFailureResponse) =
            AiLocalizationImportValidationResult(null, failure)
    }
}
