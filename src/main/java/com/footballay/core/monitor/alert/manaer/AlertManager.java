package com.footballay.core.monitor.alert.manaer;

import java.time.Duration;

public interface AlertManager {
    /**
     * 알림을 전송합니다.
     * 구현시 "중복 알림 방지" 및 "알림 전송" 을 적절히 처리해야 합니다.
     *
     * @param category 알림 카테고리
     * @param severity 알림 심각도
     * @param entityId 도메인 식별자 (fixtureId, matchId 등)
     * @param message  전송할 메시지
     * @param ttl      중복 방지 기간
     */
    void alertOnce(
            AlertCategory category,
            AlertSeverity severity,
            String entityId,
            String message,
            Duration ttl
    );
}
