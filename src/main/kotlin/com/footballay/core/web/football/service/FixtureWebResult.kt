package com.footballay.core.web.football.service

import com.footballay.core.common.result.DomainFail

sealed interface FixtureWebResult {
    data class Ok(
        val snapshotJson: String,
        val etagHash: String,
    ) : FixtureWebResult

    data class NotModified(
        val etagHash: String,
    ) : FixtureWebResult

    data class Fail(
        val error: DomainFail,
    ) : FixtureWebResult
}
