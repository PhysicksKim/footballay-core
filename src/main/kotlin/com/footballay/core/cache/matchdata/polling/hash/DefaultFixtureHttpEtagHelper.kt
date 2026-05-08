package com.footballay.core.cache.matchdata.polling.hash

import org.springframework.stereotype.Component

@Component
class DefaultFixtureHttpEtagHelper : FixtureHttpEtagHelper {
    override fun toWeakEtag(etagHash: String): String = """W/"$etagHash""""

    override fun matchesIfNoneMatch(
        ifNoneMatchHeader: String?,
        etagHash: String,
    ): Boolean {
        if (ifNoneMatchHeader.isNullOrBlank()) {
            return false
        }

        return ifNoneMatchHeader
            .split(',')
            .asSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .map(::normalizeEtagToken)
            .any { it == WILDCARD || it == etagHash }
    }

    private fun normalizeEtagToken(token: String): String {
        if (token == WILDCARD) {
            return token
        }

        val withoutWeakPrefix =
            if (token.startsWith(WEAK_PREFIX, ignoreCase = true)) {
                token.substring(WEAK_PREFIX.length).trim()
            } else {
                token
            }

        return withoutWeakPrefix.removeSurrounding("\"")
    }

    private companion object {
        const val WEAK_PREFIX = "W/"
        const val WILDCARD = "*"
    }
}
