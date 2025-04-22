package com.gyechunsik.scoreboard.web.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CachedApiResponseService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final StringRedisTemplate stringRedisTemplate;

    public Optional<String> getCachedResponseIfExist(String requestUrl, Map<String, ? extends String> parameters) {
        String key = generateKey(requestUrl, parameters);
        String cachedResponse = stringRedisTemplate.opsForValue().get(key);
        return Optional.ofNullable(cachedResponse);
    }

    public void cacheResponse(String requestUrl, Map<String, ? extends String> parameters, Object response) throws JsonProcessingException {
        String key = generateKey(requestUrl, parameters);
        stringRedisTemplate.opsForValue().set(key, OBJECT_MAPPER.writeValueAsString(new Object[]{response}), Duration.ofSeconds(10));
        log.info("Cached response for key: {}", key);
    }

    private String generateKey(String requestUrl, Map<String, ? extends String> parameters) {
        StringBuilder keyBuilder = new StringBuilder(requestUrl);
        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach((k, v) -> keyBuilder.append("&").append(k).append("=").append(v));
        }
        return keyBuilder.toString();
    }

}
