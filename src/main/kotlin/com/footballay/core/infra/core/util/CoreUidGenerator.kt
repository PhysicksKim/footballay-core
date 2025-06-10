package com.footballay.core.infra.core.util

import org.springframework.stereotype.Component
import java.security.SecureRandom

@Component
class CoreUidGenerator : UidGenerator {

    companion object {
        private val random = SecureRandom()
        const val allowedChars = "abcdefghijklmnopqrstuvwxyz0123456789"
        const val length = 16
    }

    override fun generateUid(): String {
        return buildString {
            repeat(length) {
                append(allowedChars[random.nextInt(allowedChars.length)])
            }
        }
    }

    override fun isValidUid(uid: String): Boolean {
        return uid.length == length && uid.all { it in allowedChars }
    }
}