package com.footballay.core.localization

/** Locale별 이름의 fallback을 적용합니다. */
object LocalizedNameResolver {
    fun resolve(
        requestedName: String?,
        requestedShortName: String?,
        englishName: String?,
        englishShortName: String?,
        defaultName: String,
    ): LocalizedName =
        LocalizedName(
            name = requestedName.nonBlankOrNull() ?: englishName.nonBlankOrNull() ?: defaultName,
            shortName = requestedShortName.nonBlankOrNull() ?: englishShortName.nonBlankOrNull(),
        )

    private fun String?.nonBlankOrNull(): String? = this?.takeUnless(String::isBlank)
}
