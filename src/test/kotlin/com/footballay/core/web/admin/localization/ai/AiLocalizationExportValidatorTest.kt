package com.footballay.core.web.admin.localization.ai

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/** AI export 요청의 선택값 검증 계약을 확인합니다. */
class AiLocalizationExportValidatorTest {
    private val validator = AiLocalizationExportValidator()

    @Test
    @DisplayName("지원 locale과 고유 UID 선택을 허용한다")
    fun validate_acceptsSupportedLocalesAndUniqueUids() {
        val result = validator.validate(request(locales = listOf("en", "ko"), uids = listOf("team-1", "team-2")))

        assertThat((result as DomainResult.Success).value).containsExactly(SupportedLocale.EN, SupportedLocale.KO)
    }

    @Test
    @DisplayName("비어 있거나 중복되었거나 지원하지 않는 선택을 거부한다")
    fun validate_rejectsInvalidSelections() {
        val empty = validator.validate(request(locales = emptyList(), uids = emptyList()))
        val invalid = validator.validate(request(locales = listOf("ko", "ko", "fr"), uids = listOf("team-1", "team-1")))

        assertThat((empty as DomainResult.Fail).error.validationErrors()).containsExactly("LOCALES_EMPTY", "UIDS_EMPTY")
        assertThat((invalid as DomainResult.Fail).error.validationErrors())
            .containsExactly("LOCALES_DUPLICATED", "UNSUPPORTED_LOCALE", "UIDS_DUPLICATED")
    }

    private fun request(
        locales: List<String>,
        uids: List<String>,
    ) = AiLocalizationExportRequest(AiLocalizationEntityType.TEAM, "league-1", locales = locales, uids = uids)

    private fun DomainFail.validationErrors() = (this as DomainFail.Validation).errors.map { it.code }
}
