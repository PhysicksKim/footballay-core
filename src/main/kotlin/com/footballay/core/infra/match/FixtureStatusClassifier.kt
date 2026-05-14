package com.footballay.core.infra.match

import com.footballay.core.infra.persistence.core.entity.FixtureStatusCode
import org.springframework.stereotype.Component
import java.time.Instant

enum class FixtureStatusGroup {
    PENDING,
    LIVE,
    NORMAL_FINISHED,
    NOT_PLAYED,
    UNKNOWN,
}

enum class FixtureSyncPhase {
    PRE_MATCH,
    LIVE,
    POST_MATCH,
    NOT_PLAYED,
}

@Component
class FixtureStatusClassifier {
    fun groupOf(status: FixtureStatusCode?): FixtureStatusGroup =
        when (status) {
            FixtureStatusCode.TBD,
            FixtureStatusCode.NS,
            -> FixtureStatusGroup.PENDING

            FixtureStatusCode.FIRST_HALF,
            FixtureStatusCode.HT,
            FixtureStatusCode.SECOND_HALF,
            FixtureStatusCode.ET,
            FixtureStatusCode.BT,
            FixtureStatusCode.P,
            FixtureStatusCode.SUSP,
            FixtureStatusCode.INT,
            FixtureStatusCode.LIVE,
            -> FixtureStatusGroup.LIVE

            FixtureStatusCode.FT,
            FixtureStatusCode.AET,
            FixtureStatusCode.PEN,
            -> FixtureStatusGroup.NORMAL_FINISHED

            FixtureStatusCode.PST,
            FixtureStatusCode.CANC,
            FixtureStatusCode.ABD,
            FixtureStatusCode.AWD,
            FixtureStatusCode.WO,
            -> FixtureStatusGroup.NOT_PLAYED

            null -> FixtureStatusGroup.UNKNOWN
        }

    fun determineSyncPhase(
        status: FixtureStatusCode?,
        kickoff: Instant?,
        now: Instant,
    ): FixtureSyncPhase =
        when (groupOf(status)) {
            FixtureStatusGroup.PENDING -> {
                if (status == FixtureStatusCode.NS && kickoff != null && now.isAfter(kickoff)) {
                    FixtureSyncPhase.LIVE
                } else {
                    FixtureSyncPhase.PRE_MATCH
                }
            }
            FixtureStatusGroup.LIVE -> FixtureSyncPhase.LIVE
            FixtureStatusGroup.NORMAL_FINISHED -> FixtureSyncPhase.POST_MATCH
            FixtureStatusGroup.NOT_PLAYED -> FixtureSyncPhase.NOT_PLAYED
            FixtureStatusGroup.UNKNOWN -> FixtureSyncPhase.PRE_MATCH
        }
}
