package com.gyechunsik.scoreboard.domain.football.data.cache.date.entity;

import com.gyechunsik.scoreboard.domain.football.constant.TeamId;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@Slf4j
class JsonFieldConverterTest {

    private final JsonFieldConverter jsonFieldConverter = new JsonFieldConverter();

    @DisplayName("Parameter Map 을 DB 에 저장하기 위하여 String 으로 변환합니다")
    @Test
    void Success_convert() {
        // given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("teamId", TeamId.MANUTD);
        parameters.put("season", 2023);

        // when
        String converted = jsonFieldConverter.convertToDatabaseColumn(parameters);
        log.info("Original Parameter Map :: {}", parameters);
        log.info("Converted Parameter :: {}", converted);

        // then
        assertThat(converted)
                .withFailMessage("값들이 정상적으로 포함되어 있지 않습니다.")
                .contains("teamId", Long.toString(TeamId.MANUTD), "season", Integer.toString(2023));
        assertThat(converted)
                .withFailMessage("알파벳 순으로 정렬되어 convert 되어야 합니다")
                .startsWith("{\"season\"");
    }

    @DisplayName("Parameter Map -> DB String -> Parameter Map 으로 변환에 성공합니다")
    @Test
    void Success_convertToStringAndBackToMap() {
        // given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("teamId", TeamId.MANUTD);
        parameters.put("season", 2023);

        // when
        String converted = jsonFieldConverter.convertToDatabaseColumn(parameters);
        Map<String, Object> convertBackToEntity = jsonFieldConverter.convertToEntityAttribute(converted);
        log.info("Original Parameter Map :: {}", parameters);
        log.info("Converted Parameter :: {}", converted);
        log.info("Convert back to Map :: {}", convertBackToEntity);

        // then
        assertThat(converted)
                .withFailMessage("값들이 정상적으로 포함되어 있지 않습니다.")
                .contains("teamId", Long.toString(TeamId.MANUTD), "season", Integer.toString(2023));
        assertThat(converted)
                .withFailMessage("알파벳 순으로 정렬되어 convert 되어야 합니다")
                .startsWith("{\"season\"");
        assertThat(convertBackToEntity)
                .containsKeys("teamId","season")
                .containsValues((int)TeamId.MANUTD,2023);
    }
}