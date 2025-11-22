package com.footballay.core.infra.core.dto

import com.footballay.core.infra.persistence.core.entity.FixtureStatusCode
import java.time.Instant

/**
 * FixtureCore 업데이트에 필요한 Core 전용 DTO
 *
 * API 계층의 명세에 의존하지 않는 순수한 Core 계층 DTO입니다.
 */
data class FixtureCoreUpdateDto(
    val kickoff: Instant?,
    val status: String?,
    val statusShort: FixtureStatusCode?,
    val elapsedMin: Int?,
    val goalsHome: Int?,
    val goalsAway: Int?,
    val finished: Boolean?,
    val available: Boolean?,
)
