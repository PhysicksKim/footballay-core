package com.footballay.core.infra.uid

interface UidGenerator {
    fun generateUid(): String

    fun isValidUid(uid: String): Boolean
}
