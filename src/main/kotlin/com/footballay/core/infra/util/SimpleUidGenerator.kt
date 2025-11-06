package com.footballay.core.infra.util

import org.springframework.stereotype.Component
import java.security.SecureRandom

@Component
class SimpleUidGenerator : UidGenerator {
    companion object {
        private val random = SecureRandom()
        const val ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789"
        const val UID_LENGTH = 16
    }

    override fun generateUid(): String =
        buildString {
            repeat(UID_LENGTH) {
                append(ALLOWED_CHARS[random.nextInt(ALLOWED_CHARS.length)])
            }
        }

    override fun isValidUid(uid: String): Boolean = uid.length == UID_LENGTH && uid.all { it in ALLOWED_CHARS }
}
