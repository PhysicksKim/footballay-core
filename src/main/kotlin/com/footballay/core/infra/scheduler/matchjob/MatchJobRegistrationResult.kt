package com.footballay.core.infra.scheduler.matchjob

sealed class MatchJobRegistrationResult {
    abstract val success: Boolean

    data object Registered : MatchJobRegistrationResult() {
        override val success: Boolean = true
    }

    data object Replaced : MatchJobRegistrationResult() {
        override val success: Boolean = true
    }

    data object Unchanged : MatchJobRegistrationResult() {
        override val success: Boolean = true
    }

    data class Failed(
        val message: String,
    ) : MatchJobRegistrationResult() {
        override val success: Boolean = false
    }
}
