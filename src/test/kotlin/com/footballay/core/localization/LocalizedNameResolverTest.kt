package com.footballay.core.localization

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LocalizedNameResolverTest {
    @Test
    fun `requested locale values take precedence`() {
        val result =
            LocalizedNameResolver.resolve(
                requestedName = "요청 이름",
                requestedShortName = "요청",
                englishName = "English Name",
                englishShortName = "English",
                defaultName = "Default Name",
            )

        assertThat(result).isEqualTo(LocalizedName(name = "요청 이름", shortName = "요청"))
    }

    @Test
    fun `missing requested fields fall back independently to English`() {
        val missingName =
            LocalizedNameResolver.resolve(
                requestedName = null,
                requestedShortName = "요청",
                englishName = "English Name",
                englishShortName = "English",
                defaultName = "Default Name",
            )
        val missingShortName =
            LocalizedNameResolver.resolve(
                requestedName = "요청 이름",
                requestedShortName = " ",
                englishName = "English Name",
                englishShortName = "English",
                defaultName = "Default Name",
            )

        assertThat(missingName).isEqualTo(LocalizedName(name = "English Name", shortName = "요청"))
        assertThat(missingShortName).isEqualTo(LocalizedName(name = "요청 이름", shortName = "English"))
    }

    @Test
    fun `missing localization name falls back to default name`() {
        val result =
            LocalizedNameResolver.resolve(
                requestedName = "\t",
                requestedShortName = null,
                englishName = " ",
                englishShortName = null,
                defaultName = "Default Name",
            )

        assertThat(result).isEqualTo(LocalizedName(name = "Default Name", shortName = null))
    }

    @Test
    fun `valid localization values preserve surrounding whitespace`() {
        val result =
            LocalizedNameResolver.resolve(
                requestedName = " Localized Name ",
                requestedShortName = " Short ",
                englishName = null,
                englishShortName = null,
                defaultName = "Default Name",
            )

        assertThat(result).isEqualTo(LocalizedName(name = " Localized Name ", shortName = " Short "))
    }
}
