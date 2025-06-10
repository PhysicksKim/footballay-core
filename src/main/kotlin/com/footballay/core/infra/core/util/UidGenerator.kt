package com.footballay.core.infra.core.util

interface UidGenerator {
    fun generateUid(): String
    fun isValidUid(uid: String): Boolean
}