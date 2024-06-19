package com.gyechunsik.scoreboard.domain.football.external.lastlog;

import com.gyechunsik.scoreboard.domain.football.entity.apicache.LastCacheLog;
import com.gyechunsik.scoreboard.domain.football.entity.apicache.ApiCacheType;
import com.gyechunsik.scoreboard.domain.football.repository.apicache.LastCacheLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class LastCacheLogService {

    private final LastCacheLogRepository lastCacheLogRepository;

    /**
     * 신규 LastCacheLog 엔티티를 저장하거나, 기존의 LastCacheLog 의 마지막 캐싱 시점을 업데이트합니다.
     * @param type
     * @param parameter
     * @param cachedAt
     * @return
     */
    public LastCacheLog saveApiCache(ApiCacheType type, Map<String, Object> parameter, ZonedDateTime cachedAt) {
        Optional<LastCacheLog> findApiCache = lastCacheLogRepository.findLastCacheLogByApiCacheTypeAndParametersJson(type, parameter);
        LastCacheLog lastCacheLog = findApiCache.orElseGet(() -> LastCacheLog.builder().apiCacheType(type).parametersJson(parameter).build());
        lastCacheLog.setLastCachedAt(cachedAt);
        lastCacheLogRepository.save(lastCacheLog);
        return lastCacheLog;
    }

    public LastCacheLog findApiCache(ApiCacheType type, Map<String, Object> parameters) {
        Optional<LastCacheLog> findApiCache = lastCacheLogRepository.findLastCacheLogByApiCacheTypeAndParametersJson(type, parameters);
        LastCacheLog lastCacheLog = findApiCache.orElseThrow(()
                -> new IllegalArgumentException("일치하는 Api Cache 를 찾지 못했습니다.\n" +
                "type=" + type + ",parameters=" + parameters.toString()));
        log.info("find api cache : {}", lastCacheLog);
        return lastCacheLog;
    }

}
