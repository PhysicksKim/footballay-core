package com.footballay.core.infra.persistence.core.entity

/**
 * Fixture 상태 약어 Enum
 * 
 * ApiSports API에서 제공하는 모든 경기 상태값을 포함합니다.
 * 
 * @author Footballay Core Team
 * @since 1.0.0
 */
enum class FixtureStatusShort(val value: String) {
    TBD("TBD"),                    // Time To Be Defined
    NS("NS"),                      // Not Started
    FIRST_HALF("1H"),              // First Half, Kick Off
    HT("HT"),                      // Halftime
    SECOND_HALF("2H"),             // Second Half, 2nd Half Started
    ET("ET"),                      // Extra Time
    BT("BT"),                      // Break Time
    P("P"),                        // Penalty In Progress
    SUSP("SUSP"),                  // Match Suspended
    INT("INT"),                    // Match Interrupted
    FT("FT"),                      // Match Finished
    AET("AET"),                    // Match Finished (after extra time)
    PEN("PEN"),                    // Match Finished (after penalty)
    PST("PST"),                    // Match Postponed
    CANC("CANC"),                  // Match Cancelled
    ABD("ABD"),                    // Match Abandoned
    AWD("AWD"),                    // Technical Loss
    WO("WO"),                      // WalkOver
    LIVE("LIVE");                  // In Progress

    companion object {
        fun fromString(value: String?): FixtureStatusShort? {
            return values().find { it.value == value }
        }

        fun isLiveStatus(status: FixtureStatusShort): Boolean {
            return status in setOf(
                FIRST_HALF, HT, SECOND_HALF, ET, BT, P, SUSP, INT, FT, AET, PEN, LIVE
            )
        }
    }
} 