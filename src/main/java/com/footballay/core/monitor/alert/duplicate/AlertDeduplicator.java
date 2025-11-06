package com.footballay.core.monitor.alert.duplicate;

import java.time.Duration;

/**
 * 알림 중복 제어
 */
public interface AlertDeduplicator {
    /**
     * 최초 호출 시에만 true 반환, 이후 false
     * @param type 알림 구분자
     * @param id   고유 식별자
     * @param ttl  중복 방지 기간
     */
    boolean shouldNotify(String type, String id, Duration ttl);

    /**
     * 중복 제어 키 삭제
     * @param type 알림 구분자
     * @param id   고유 식별자
     */
    void invalidate(String type, String id);
}
