package com.footballay.core.domain.football.scheduler;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class MatchTimeUtil {

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final ZoneId UTC = ZoneId.of("UTC");

    /**
     * 기본 사용: 시스템 UTC 시각을 기준으로 계산
     */
    public static boolean beforeStart10Minutes(String matchStartTime) {
        return beforeStart10Minutes(matchStartTime, OffsetDateTime.now(UTC));
    }

    /**
     * 테스트나 커스텀 오프셋이 필요할 때 사용하는 오버로드 메서드
     *
     * @param matchStartTime ISO_OFFSET_DATE_TIME 형식 (예: "2024-08-16T19:00:00+00:00")
     * @param currentTime    기준이 되는 현재 시각 (원하는 Zone/Offset 그대로 전달)
     * @return 경기 시작 10분 전 또는 그 이하인지 여부
     */
    public static boolean beforeStart10Minutes(String matchStartTime,
                                               OffsetDateTime currentTime) {
        Objects.requireNonNull(matchStartTime,  "matchStartTime must not be null");
        Objects.requireNonNull(currentTime,     "currentTime must not be null");

        OffsetDateTime matchStart = OffsetDateTime.parse(matchStartTime, ISO_FMT);
        Duration duration = Duration.between(currentTime, matchStart);
        long minutes = duration.toMinutes();
        return minutes <= 10 && minutes > 0;
    }
}
