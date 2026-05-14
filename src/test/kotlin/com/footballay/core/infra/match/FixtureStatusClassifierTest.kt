package com.footballay.core.infra.match

import com.footballay.core.infra.persistence.core.entity.FixtureStatusCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class FixtureStatusClassifierTest {
    private val classifier = FixtureStatusClassifier()

    @Test
    fun `known status code를 group으로 분류한다`() {
        val expected =
            mapOf(
                FixtureStatusCode.TBD to FixtureStatusGroup.PENDING,
                FixtureStatusCode.NS to FixtureStatusGroup.PENDING,
                FixtureStatusCode.FIRST_HALF to FixtureStatusGroup.LIVE,
                FixtureStatusCode.HT to FixtureStatusGroup.LIVE,
                FixtureStatusCode.SECOND_HALF to FixtureStatusGroup.LIVE,
                FixtureStatusCode.ET to FixtureStatusGroup.LIVE,
                FixtureStatusCode.BT to FixtureStatusGroup.LIVE,
                FixtureStatusCode.P to FixtureStatusGroup.LIVE,
                FixtureStatusCode.SUSP to FixtureStatusGroup.LIVE,
                FixtureStatusCode.INT to FixtureStatusGroup.LIVE,
                FixtureStatusCode.LIVE to FixtureStatusGroup.LIVE,
                FixtureStatusCode.FT to FixtureStatusGroup.NORMAL_FINISHED,
                FixtureStatusCode.AET to FixtureStatusGroup.NORMAL_FINISHED,
                FixtureStatusCode.PEN to FixtureStatusGroup.NORMAL_FINISHED,
                FixtureStatusCode.PST to FixtureStatusGroup.NOT_PLAYED,
                FixtureStatusCode.CANC to FixtureStatusGroup.NOT_PLAYED,
                FixtureStatusCode.ABD to FixtureStatusGroup.NOT_PLAYED,
                FixtureStatusCode.AWD to FixtureStatusGroup.NOT_PLAYED,
                FixtureStatusCode.WO to FixtureStatusGroup.NOT_PLAYED,
            )

        FixtureStatusCode.entries.forEach { status ->
            assertThat(classifier.groupOf(status))
                .describedAs("status=$status")
                .isEqualTo(expected.getValue(status))
        }
    }

    @Test
    fun `NS이고 kickoff이 지났으면 live phase로 판단한다`() {
        val now = Instant.parse("2026-05-14T10:00:00Z")
        val kickoff = now.minusSeconds(60)

        val phase = classifier.determineSyncPhase(FixtureStatusCode.NS, kickoff, now)

        assertThat(phase).isEqualTo(FixtureSyncPhase.LIVE)
    }

    @Test
    fun `NS이고 kickoff이 미래면 pre match phase로 판단한다`() {
        val now = Instant.parse("2026-05-14T10:00:00Z")
        val kickoff = now.plusSeconds(60)

        val phase = classifier.determineSyncPhase(FixtureStatusCode.NS, kickoff, now)

        assertThat(phase).isEqualTo(FixtureSyncPhase.PRE_MATCH)
    }

    @Test
    fun `FT AET PEN은 post match phase로 판단한다`() {
        listOf(FixtureStatusCode.FT, FixtureStatusCode.AET, FixtureStatusCode.PEN).forEach { status ->
            assertThat(classifier.determineSyncPhase(status, null, Instant.now()))
                .describedAs("status=$status")
                .isEqualTo(FixtureSyncPhase.POST_MATCH)
        }
    }

    @Test
    fun `PST CANC ABD AWD WO는 not played phase로 판단한다`() {
        listOf(
            FixtureStatusCode.PST,
            FixtureStatusCode.CANC,
            FixtureStatusCode.ABD,
            FixtureStatusCode.AWD,
            FixtureStatusCode.WO,
        ).forEach { status ->
            assertThat(classifier.determineSyncPhase(status, null, Instant.now()))
                .describedAs("status=$status")
                .isEqualTo(FixtureSyncPhase.NOT_PLAYED)
        }
    }

    @Test
    fun `INT SUSP는 live phase로 판단한다`() {
        listOf(FixtureStatusCode.INT, FixtureStatusCode.SUSP).forEach { status ->
            assertThat(classifier.determineSyncPhase(status, null, Instant.now()))
                .describedAs("status=$status")
                .isEqualTo(FixtureSyncPhase.LIVE)
        }
    }
}
