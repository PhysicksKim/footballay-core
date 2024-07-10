package com.gyechunsik.scoreboard.utils;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class TimeConverter {

    public static OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime, String timezone) {
        return ZonedDateTime.of(localDateTime, ZoneId.of(timezone)).toOffsetDateTime();
    }

}
