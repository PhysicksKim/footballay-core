package com.footballay.core.domain.football.external.lastlog;

import com.footballay.core.domain.football.constant.LeagueId;
import com.footballay.core.domain.football.persistence.apicache.ApiCacheType;
import com.footballay.core.domain.football.persistence.apicache.LastCacheLog;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles("mockapi")
@SpringBootTest
class LastCacheLogServiceTest {

    @Autowired
    private LastCacheLogService lastCacheLogService;

    @DisplayName("Service 를 통해 LastCacheLog 를 생성하고 엔티티를 신규로 저장합니다")
    @Test
    void success_saveApiCache() {
        // given
        ApiCacheType type = ApiCacheType.LEAGUE;
        Map<String, Object> parameters = Map.of("leagueId", (int) LeagueId.EPL);
        ZonedDateTime cachedAt = ZonedDateTime.now();

        // when
        LastCacheLog lastCacheLog = lastCacheLogService.saveApiCache(type, parameters, cachedAt);
        log.info("Cache Entity :: {}", lastCacheLog);

        // then
        assertThat(lastCacheLog).isNotNull();
        assertThat(lastCacheLog.getApiCacheType()).isEqualTo(type);
        assertThat(lastCacheLog.getParametersJson()).isEqualTo(parameters);
        assertThat(lastCacheLog.getLastCachedAt()).isEqualTo(cachedAt);
    }

    @DisplayName("LastCacheLog 타입 파라미터가 동일한 경우 마지막 캐싱 시간을 업데이트 합니다")
    @Test
    void success_updateApiCacheLastCacheDate() {
        // given
        ApiCacheType type1 = ApiCacheType.LEAGUE;
        Map<String, Object> beforeParams = Map.of("leagueId", (int) LeagueId.EPL);
        ZonedDateTime beforeCachedAt = ZonedDateTime.now().minusDays(1).minusHours(3);
        LastCacheLog beforeLastCacheLog = lastCacheLogService.saveApiCache(type1, beforeParams, beforeCachedAt);

        // when
        ApiCacheType type2 = ApiCacheType.LEAGUE;
        Map<String, Object> afterParams = Map.of("leagueId", (int) LeagueId.EPL);
        ZonedDateTime afterCachedAt = ZonedDateTime.now();
        LastCacheLog afterLastCacheLog = lastCacheLogService.saveApiCache(type2, afterParams, afterCachedAt);

        log.info("before Cache :: {}", beforeLastCacheLog);
        log.info("after Cache :: {}", afterLastCacheLog);

        // then
        assertThat(beforeLastCacheLog).isEqualTo(afterLastCacheLog);
        Assertions.assertFalse(afterLastCacheLog.equalsWithTime(beforeLastCacheLog));
        assertThat(beforeLastCacheLog.getLastCachedAt()).isEqualTo(beforeCachedAt);
        assertThat(afterLastCacheLog.getLastCachedAt()).isEqualTo(afterCachedAt);
    }

    @DisplayName("Parameter 가 빈 Map 인 LastCacheLog 를 생성하고 저장합니다")
    @Test
    void success_EmptyParameterMapApiCache() {
        // given
        ApiCacheType type = ApiCacheType.CURRENT_LEAGUES;
        Map<String, Object> emptyParams = Map.of();
        ZonedDateTime cachedAt = ZonedDateTime.now();

        // when
        LastCacheLog lastCacheLog = lastCacheLogService.saveApiCache(type, emptyParams, cachedAt);
        log.info("Cache Entity :: {}", lastCacheLog);

        // then
        assertThat(lastCacheLog).isNotNull();
        assertThat(lastCacheLog.getApiCacheType()).isEqualTo(type);
        assertThat(lastCacheLog.getParametersJson()).isEqualTo(emptyParams);
        assertThat(lastCacheLog.getLastCachedAt()).isEqualTo(cachedAt);
    }


}