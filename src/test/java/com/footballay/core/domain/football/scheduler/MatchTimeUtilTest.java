package com.footballay.core.domain.football.scheduler;

import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class MatchTimeUtilTest {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Test
    void within10Minutes_beforeStart_returnsTrue() {
        // currentTime: 2025-07-10T09:51:00+00:00 (9분 전)
        OffsetDateTime currentTime = OffsetDateTime.parse("2025-07-10T09:51:00+00:00", FMT);
        String matchStart = "2025-07-10T10:00:00+00:00";

        assertTrue(
                MatchTimeUtil.beforeStart10Minutes(matchStart, currentTime),
                "9분 전이므로 true 여야 합니다"
        );
    }

    @Test
    void exactly10MinutesBefore_returnsTrue() {
        // currentTime: 2025-07-10T09:50:00+00:00 (10분 전)
        OffsetDateTime currentTime = OffsetDateTime.parse("2025-07-10T09:50:00+00:00", FMT);
        String matchStart = "2025-07-10T10:00:00+00:00";

        assertTrue(
                MatchTimeUtil.beforeStart10Minutes(matchStart, currentTime),
                "정확히 10분 전이므로 true 여야 합니다"
        );
    }

    @Test
    void moreThan10MinutesBefore_returnsFalse() {
        // currentTime: 2025-07-10T09:49:00+00:00 (11분 전)
        OffsetDateTime currentTime = OffsetDateTime.parse("2025-07-10T09:49:00+00:00", FMT);
        String matchStart = "2025-07-10T10:00:00+00:00";

        assertFalse(
                MatchTimeUtil.beforeStart10Minutes(matchStart, currentTime),
                "10분 초과 전이므로 false 여야 합니다"
        );
    }

    @Test
    void atOrAfterStartTime_returnsFalse() {
        // 정확히 시작 시각
        OffsetDateTime atStart = OffsetDateTime.parse("2025-07-10T10:00:00+00:00", FMT);
        String matchStart = "2025-07-10T10:00:00+00:00";

        assertFalse(
                MatchTimeUtil.beforeStart10Minutes(matchStart, atStart),
                "시작 시각에는 false 여야 합니다"
        );

        // 이미 시작 이후
        OffsetDateTime afterStart = OffsetDateTime.parse("2025-07-10T10:01:00+00:00", FMT);
        assertFalse(
                MatchTimeUtil.beforeStart10Minutes(matchStart, afterStart),
                "이미 지난 시각에는 false 여야 합니다"
        );
    }

    @Test
    void nullMatchStartTime_throwsException() {
        OffsetDateTime currentTime = OffsetDateTime.parse("2025-07-10T09:50:00+00:00", FMT);

        assertThrows(
                NullPointerException.class,
                () -> MatchTimeUtil.beforeStart10Minutes(null, currentTime),
                "matchStartTime이 null이면 NullPointerException이 발생해야 합니다"
        );
    }

    @Test
    void nullCurrentTime_throwsException() {
        String matchStart = "2025-07-10T10:00:00+00:00";

        assertThrows(
                NullPointerException.class,
                () -> MatchTimeUtil.beforeStart10Minutes(matchStart, null),
                "currentTime이 null이면 NullPointerException이 발생해야 합니다"
        );
    }
}
