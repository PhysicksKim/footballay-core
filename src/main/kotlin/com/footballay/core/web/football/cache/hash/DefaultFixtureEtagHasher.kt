package com.footballay.core.web.football.cache.hash

import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.Base64

@Component
class DefaultFixtureEtagHasher : FixtureEtagHasher {
    override fun hash(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance(SHA_256).digest(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    private companion object {
        const val SHA_256 = "SHA-256"
    }
}
