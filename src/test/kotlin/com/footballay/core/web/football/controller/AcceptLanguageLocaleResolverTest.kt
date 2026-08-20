package com.footballay.core.web.football.controller

import com.footballay.core.localization.SupportedLocale
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AcceptLanguageLocaleResolverTest {
    private val resolver = AcceptLanguageLocaleResolver()

    @Test
    fun `missing unsupported wildcard and malformed headers default to English`() {
        assertThat(resolver.resolve(null)).isEqualTo(SupportedLocale.EN)
        assertThat(resolver.resolve("fr-CA, de;q=0.8")).isEqualTo(SupportedLocale.EN)
        assertThat(resolver.resolve("*;q=0.8")).isEqualTo(SupportedLocale.EN)
        assertThat(resolver.resolve("@@@")).isEqualTo(SupportedLocale.EN)
    }

    @Test
    fun `language regions and q values select the highest supported locale`() {
        assertThat(resolver.resolve("en-US, ko-KR;q=0.9")).isEqualTo(SupportedLocale.EN)
        assertThat(resolver.resolve("en;q=0.2, ko-KR;q=0.9")).isEqualTo(SupportedLocale.KO)
        assertThat(resolver.resolve("ko;q=0, en;q=0.5")).isEqualTo(SupportedLocale.EN)
    }
}
