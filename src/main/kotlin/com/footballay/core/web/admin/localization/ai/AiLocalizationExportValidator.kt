package com.footballay.core.web.admin.localization.ai

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportRequest
import org.springframework.stereotype.Component

/** AI export 요청의 locale과 UID 목록을 검증합니다. */
@Component
class AiLocalizationExportValidator {
    fun validate(request: AiLocalizationExportRequest): DomainResult<List<SupportedLocale>, DomainFail> {
        val locales = request.locales.map { code -> SupportedLocale.entries.find { it.code == code } }
        val errors = buildList {
            if (request.locales.isEmpty()) add(error("LOCALES_EMPTY", "locales는 비어 있을 수 없습니다.", "locales"))
            if (request.locales.size != request.locales.toSet().size) add(error("LOCALES_DUPLICATED", "locales에 중복 값이 있습니다.", "locales"))
            request.locales.zip(locales).filter { (_, locale) -> locale == null }.forEach { (code, _) ->
                add(error("UNSUPPORTED_LOCALE", "지원하지 않는 locale입니다: $code", "locales"))
            }
            if (request.uids.isEmpty()) add(error("UIDS_EMPTY", "uids는 비어 있을 수 없습니다.", "uids"))
            if (request.uids.size != request.uids.toSet().size) add(error("UIDS_DUPLICATED", "uids에 중복 값이 있습니다.", "uids"))
        }
        return if (errors.isEmpty()) DomainResult.Success(locales.filterNotNull()) else DomainResult.Fail(DomainFail.Validation(errors))
    }

    private fun error(code: String, message: String, field: String) =
        DomainFail.Validation.ValidationError(code = code, message = message, field = field)
}
