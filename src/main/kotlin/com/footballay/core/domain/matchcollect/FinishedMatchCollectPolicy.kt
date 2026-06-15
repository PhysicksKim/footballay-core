package com.footballay.core.domain.matchcollect

import java.time.Duration
import java.time.Instant

class FinishedMatchCollectPolicy(
    val checkpointOffsets: List<Duration> = DEFAULT_CHECKPOINT_OFFSETS,
) {
    fun shouldCollect(
        kickoff: Instant,
        lastCollectedAt: Instant?,
        now: Instant,
    ): Boolean {
        val requiredCheckpointAt = requiredCheckpointAt(kickoff, now) ?: return false
        return lastCollectedAt == null || lastCollectedAt.isBefore(requiredCheckpointAt)
    }

    fun requiredCheckpointAt(
        kickoff: Instant,
        now: Instant,
    ): Instant? =
        checkpointOffsets
            .map { kickoff.plus(it) }
            .filter { !it.isAfter(now) }
            .maxOrNull()

    companion object {
        val DEFAULT_CHECKPOINT_OFFSETS: List<Duration> =
            listOf(
                Duration.ofHours(3),
                Duration.ofHours(5),
                Duration.ofHours(12),
            )
    }
}
