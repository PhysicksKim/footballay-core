package com.footballay.core.monitor.alert.port;

import com.footballay.core.monitor.alert.manaer.AlertCategory;
import com.footballay.core.monitor.alert.manaer.AlertManager;
import com.footballay.core.monitor.alert.manaer.AlertSeverity;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Lineup, LiveMatch, id-null 문제에 대해 알림을 보냅니다.
 * 각 세부 도메인의 요청 포인트에서 {@link AlertManager} 로 이어줍니다.
 */
@Service
public class MatchAlertServiceImpl implements MatchAlertService {

    private static final Duration DEFAULT_TTL = Duration.ofDays(1);

    private final AlertManager alertManager;

    public MatchAlertServiceImpl(AlertManager alertManager) {
        this.alertManager = alertManager;
    }

    @Override
    public void alertLineupSuccessOnce(String fixtureId, String message) {
        conveyAlert(AlertCategory.LINEUP, AlertSeverity.SUCCESS, fixtureId, message);
    }

    @Override
    public void alertLineupFailureOnce(String fixtureId, String message) {
        conveyAlert(AlertCategory.LINEUP, AlertSeverity.FAILURE, fixtureId, message);
    }

    @Override
    public void alertFixtureSuccessOnce(String fixtureId, String message) {
        conveyAlert(AlertCategory.MATCHDATA, AlertSeverity.SUCCESS, fixtureId, message);
    }

    @Override
    public void alertFixtureExceptionOnce(String fixtureId, String message) {
        conveyAlert(AlertCategory.MATCHDATA, AlertSeverity.EXCEPTION, fixtureId, message);
    }

    @Override
    public void alertIdNullWarn(String targetId, String message) {
        conveyAlert(AlertCategory.MATCHIDNULL, AlertSeverity.WARNING, targetId, message);
    }

    private void conveyAlert(AlertCategory category, AlertSeverity severity, String fixtureId, String message) {
        alertManager.alertOnce(
                category,
                severity,
                fixtureId,
                message,
                DEFAULT_TTL
        );
    }

}

