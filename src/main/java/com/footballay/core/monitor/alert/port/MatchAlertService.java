package com.footballay.core.monitor.alert.port;

/**
 * 매치 알림 서비스 추상화 인터페이스
 */
public interface MatchAlertService {
    void alertLineupSuccessOnce(String fixtureId, String message);
    void alertLineupFailureOnce(String fixtureId, String message);
    void alertFixtureSuccessOnce(String fixtureId, String message);
    void alertFixtureExceptionOnce(String fixtureId, String message);
    void alertIdNullWarn(String targetId, String message);
}

