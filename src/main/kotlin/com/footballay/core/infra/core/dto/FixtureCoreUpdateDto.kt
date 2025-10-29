package com.footballay.core.infra.core.dto

import com.footballay.core.infra.persistence.core.entity.FixtureStatusShort
import java.time.OffsetDateTime

/**
 * FixtureCore 업데이트에 필요한 Core 전용 DTO
 *
 * API 계층의 명세에 의존하지 않는 순수한 Core 계층 DTO입니다.
 */
data class FixtureCoreUpdateDto(
    val kickoff: OffsetDateTime?,
    val timestamp: Long?,
    val status: String?,
    val statusShort: FixtureStatusShort?,
    val elapsedMin: Int?,
    val goalsHome: Int?,
    val goalsAway: Int?,
    val finished: Boolean?,
    val available: Boolean?,
)
