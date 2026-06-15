package com.footballay.core.domain.matchcollect

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class FinishedMatchCollectPolicyTest {
    private val kickoff = Instant.parse("2026-06-15T00:00:00Z")

    @Test
    fun `도달한 checkpoint가 없으면 collect 대상이 아니다`() {
        val policy = policy(Duration.ofHours(5))

        assertThat(policy.shouldCollect(kickoff, null, kickoff.plus(Duration.ofHours(5)).minusNanos(1))).isFalse()
    }

    @Test
    fun `checkpoint 이후 lastCollectedAt이 없으면 collect 대상이다`() {
        val checkpoint = Duration.ofHours(5)
        val policy = policy(checkpoint)

        assertThat(policy.shouldCollect(kickoff, null, kickoff.plus(checkpoint))).isTrue()
    }

    @Test
    fun `checkpoint 직전 collect는 해당 checkpoint를 충족하지 못한다`() {
        val checkpoint = Duration.ofHours(5)
        val policy = policy(checkpoint)
        val lastCollectedAt = kickoff.plus(checkpoint).minusNanos(1)

        assertThat(policy.shouldCollect(kickoff, lastCollectedAt, kickoff.plus(checkpoint))).isTrue()
    }

    @Test
    fun `checkpoint 이후 collect는 다음 checkpoint 전까지 다시 collect하지 않는다`() {
        val firstCheckpoint = Duration.ofHours(5)
        val secondCheckpoint = Duration.ofHours(12)
        val policy = policy(firstCheckpoint, secondCheckpoint)
        val lastCollectedAt = kickoff.plus(firstCheckpoint).plusSeconds(12)

        assertThat(policy.shouldCollect(kickoff, lastCollectedAt, kickoff.plus(secondCheckpoint).minusNanos(1))).isFalse()
    }

    @Test
    fun `다음 checkpoint 이후 이전 checkpoint 수집만 되어 있으면 다시 collect 대상이다`() {
        val firstCheckpoint = Duration.ofHours(5)
        val secondCheckpoint = Duration.ofHours(12)
        val policy = policy(firstCheckpoint, secondCheckpoint)
        val lastCollectedAt = kickoff.plus(firstCheckpoint).plusSeconds(12)

        assertThat(policy.shouldCollect(kickoff, lastCollectedAt, kickoff.plus(secondCheckpoint))).isTrue()
    }

    @Test
    fun `가장 늦게 도달한 checkpoint를 기준으로 collect 여부를 판단한다`() {
        val checkpoints =
            listOf(
                Duration.ofHours(1),
                Duration.ofHours(2),
                Duration.ofHours(3),
                Duration.ofHours(4),
                Duration.ofHours(5),
                Duration.ofHours(6),
            )
        val policy = FinishedMatchCollectPolicy(checkpoints)
        val latestReachedCheckpoint = Duration.ofHours(4)
        val lastCollectedAt = kickoff.plus(Duration.ofHours(3)).plusSeconds(1)

        assertThat(policy.requiredCheckpointAt(kickoff, kickoff.plus(latestReachedCheckpoint))).isEqualTo(kickoff.plus(latestReachedCheckpoint))
        assertThat(policy.shouldCollect(kickoff, lastCollectedAt, kickoff.plus(latestReachedCheckpoint))).isTrue()
    }

    @Test
    fun `마지막으로 도달한 checkpoint 이후 collect가 되어 있으면 다시 collect하지 않는다`() {
        val checkpoints =
            listOf(
                Duration.ofHours(1),
                Duration.ofHours(2),
                Duration.ofHours(3),
                Duration.ofHours(4),
                Duration.ofHours(5),
                Duration.ofHours(6),
            )
        val policy = FinishedMatchCollectPolicy(checkpoints)
        val latestReachedCheckpoint = Duration.ofHours(4)
        val lastCollectedAt = kickoff.plus(latestReachedCheckpoint)

        assertThat(policy.shouldCollect(kickoff, lastCollectedAt, kickoff.plus(latestReachedCheckpoint).plusSeconds(1))).isFalse()
    }

    @Test
    fun `checkpoint가 비어 있으면 collect 대상이 아니다`() {
        val policy = FinishedMatchCollectPolicy(emptyList())

        assertThat(policy.shouldCollect(kickoff, null, kickoff.plus(Duration.ofDays(1)))).isFalse()
    }

    private fun policy(vararg checkpoints: Duration): FinishedMatchCollectPolicy {
        return FinishedMatchCollectPolicy(checkpoints.toList())
    }
}
