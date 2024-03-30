package com.gyechunsik.scoreboard.domain.football.data.cache.date;

import com.gyechunsik.scoreboard.domain.football.data.cache.date.entity.ApiCache;
import com.gyechunsik.scoreboard.domain.football.data.cache.date.entity.ApiCacheType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ApiCacheDateService {

    private final ApiCacheDateRepository apiCacheDateRepository;

    /**
     * 신규 ApiCache 엔티티를 저장하거나, 기존의 ApiCache 의 마지막 캐싱 시점을 업데이트합니다.
     * @param type
     * @param parameter
     * @param cachedAt
     * @return
     */
    public ApiCache saveApiCache(ApiCacheType type, Map<String, Object> parameter, LocalDateTime cachedAt) {
        Optional<ApiCache> findApiCache = apiCacheDateRepository.findApiCacheByApiCacheTypeAndParametersJson(type, parameter);
        ApiCache apiCache = findApiCache.orElseGet(() -> ApiCache.builder().apiCacheType(type).parametersJson(parameter).build());
        apiCache.setLastCachedAt(cachedAt);
        apiCacheDateRepository.save(apiCache);
        return apiCache;
    }

    public ApiCache findApiCache(ApiCacheType type, Map<String, Object> parameters) {
        Optional<ApiCache> findApiCache = apiCacheDateRepository.findApiCacheByApiCacheTypeAndParametersJson(type, parameters);
        ApiCache apiCache = findApiCache.orElseThrow(()
                -> new IllegalArgumentException("일치하는 Api Cache 를 찾지 못했습니다.\n" +
                "type=" + type + ",parameters=" + parameters.toString()));
        log.info("find api cache : {}", apiCache);
        return apiCache;
    }

}
