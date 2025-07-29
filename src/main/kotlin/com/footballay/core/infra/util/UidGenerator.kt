package com.footballay.core.infra.util

interface UidGenerator {
    fun generateUid(): String
    fun isValidUid(uid: String): Boolean
}