package com.gyechunsik.scoreboard.domain.football.data.cache.date;

import com.gyechunsik.scoreboard.domain.football.data.cache.date.entity.ApiCache;
import com.gyechunsik.scoreboard.domain.football.data.cache.date.entity.ApiCacheType;
import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@ActiveProfiles("mockapi")
@SpringBootTest
class ApiCacheDateServiceTest {

    @Autowired
    private ApiCacheDateService apiCacheDateService;

    @DisplayName("Service 를 통해 ApiCache 를 생성하고 엔티티를 신규로 저장합니다")
    @Test
    void success_saveApiCache() {
        // given
        ApiCacheType type = ApiCacheType.LEAGUE;
        Map<String, Object> parameters = Map.of("leagueId", (int) LeagueId.EPL);
        LocalDateTime cachedAt = LocalDateTime.now();

        // when
        ApiCache apiCache = apiCacheDateService.saveApiCache(type, parameters, cachedAt);
        log.info("Cache Entity :: {}", apiCache);

        // then
        assertThat(apiCache).isNotNull();
        assertThat(apiCache.getApiCacheType()).isEqualTo(type);
        assertThat(apiCache.getParametersJson()).isEqualTo(parameters);
        assertThat(apiCache.getLastCachedAt()).isEqualTo(cachedAt);
    }

    @DisplayName("ApiCache 타입 파라미터가 동일한 경우 마지막 캐싱 시간을 업데이트 합니다")
    @Test
    void success_updateApiCacheLastCacheDate() {
        // given
        ApiCacheType type1 = ApiCacheType.LEAGUE;
        Map<String, Object> beforeParams = Map.of("leagueId", (int) LeagueId.EPL);
        LocalDateTime beforeCachedAt = LocalDateTime.now().minusDays(1).minusHours(3);
        ApiCache beforeApiCache = apiCacheDateService.saveApiCache(type1, beforeParams, beforeCachedAt);

        // when
        ApiCacheType type2 = ApiCacheType.LEAGUE;
        Map<String, Object> afterParams = Map.of("leagueId", (int) LeagueId.EPL);
        LocalDateTime afterCachedAt = LocalDateTime.now();
        ApiCache afterApiCache = apiCacheDateService.saveApiCache(type2, afterParams, afterCachedAt);

        log.info("before Cache :: {}", beforeApiCache);
        log.info("after Cache :: {}", afterApiCache);

        // then
        assertThat(beforeApiCache).isEqualTo(afterApiCache);
        Assertions.assertFalse(afterApiCache.equalsWithTime(beforeApiCache));
        assertThat(beforeApiCache.getLastCachedAt()).isEqualTo(beforeCachedAt);
        assertThat(afterApiCache.getLastCachedAt()).isEqualTo(afterCachedAt);
    }

    @DisplayName("Parameter 가 빈 Map 인 ApiCache 를 생성하고 저장합니다")
    @Test
    void success_EmptyParameterMapApiCache() {
        // given
        ApiCacheType type = ApiCacheType.CURRENT_LEAGUES;
        Map<String, Object> emptyParams = Map.of();
        LocalDateTime cachedAt = LocalDateTime.now();

        // when
        ApiCache apiCache = apiCacheDateService.saveApiCache(type, emptyParams, cachedAt);
        log.info("Cache Entity :: {}", apiCache);

        // then
        assertThat(apiCache).isNotNull();
        assertThat(apiCache.getApiCacheType()).isEqualTo(type);
        assertThat(apiCache.getParametersJson()).isEqualTo(emptyParams);
        assertThat(apiCache.getLastCachedAt()).isEqualTo(cachedAt);
    }


}