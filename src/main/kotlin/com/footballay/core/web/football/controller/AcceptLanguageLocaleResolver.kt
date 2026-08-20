package com.footballay.core.web.football.controller

import com.footballay.core.localization.SupportedLocale
import org.springframework.stereotype.Component
import java.util.Locale

/** Accept-Language 헤더를 Footballay 지원 locale로 정규화합니다. */
@Component
class AcceptLanguageLocaleResolver {
    fun resolve(header: String?): SupportedLocale {
        val rawHeader = header?.takeIf { it.isNotBlank() } ?: return SupportedLocale.EN
        val ranges =
            try {
                Locale.LanguageRange.parse(rawHeader)
            } catch (_: IllegalArgumentException) {
                return SupportedLocale.EN
            }

        val locale =
            ranges
            .asSequence()
            .filter { it.weight > 0.0 }
            .mapNotNull { range ->
                val languageRange = range.range.lowercase(Locale.ROOT)
                when {
                    languageRange == "en" || languageRange.startsWith("en-") -> SupportedLocale.EN
                    languageRange == "ko" || languageRange.startsWith("ko-") -> SupportedLocale.KO
                    else -> null
                }
            }.firstOrNull()

        return locale ?: SupportedLocale.EN
    }
}
