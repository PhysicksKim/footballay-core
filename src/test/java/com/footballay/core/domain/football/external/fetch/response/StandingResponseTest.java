package com.footballay.core.domain.football.external.fetch.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class StandingResponseTest {

    private static final Logger log = LoggerFactory.getLogger(StandingResponseTest.class);

    @DisplayName("")
    @Test
    void dateParseTest() {
        // given
        String dateString = "2025-03-17T00:00:00+00:00";
        // when
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateString);
        // then
        log.info("offsetDateTime: {}", offsetDateTime);
        assertEquals(2025, offsetDateTime.getYear());
        assertEquals(3, offsetDateTime.getMonthValue());
        assertEquals(17, offsetDateTime.getDayOfMonth());

    }

}