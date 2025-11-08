package com.footballay.core.monitor.alert.notify;

import com.footballay.core.monitor.alert.NotificationException;
import com.footballay.core.monitor.alert.manaer.AlertSeverity;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles({"dev", "devrealapi"})
@Disabled("실제 디스코드 웹훅으로 알림을 보내는 통합 테스트입니다.")
// @TestPropertySource(properties = "discord.debug-mode=true") // 실제로 알림을 보내고 싶다면 주석을 해제하세요
class MatchIdNullAlertNotifierTest {

    @Autowired
    private MatchIdNullAlertNotifier notifier;

    @Test
    void sendSuccessWebhook_integration() throws NotificationException {
        notifier.notifyAlert(
                AlertSeverity.SUCCESS,
                "idn-001",
                "[TEST] MatchIdNull success webhook"
        );
    }

    @Test
    void sendFailureWebhook_integration() throws NotificationException {
        notifier.notifyAlert(
                AlertSeverity.FAILURE,
                "idn-002",
                "[TEST] MatchIdNull failure webhook"
        );
    }

    @Test
    void sendExceptionWebhook_integration() throws NotificationException {
        notifier.notifyAlert(
                AlertSeverity.EXCEPTION,
                "idn-003",
                "[TEST] MatchIdNull exception webhook"
        );
    }

    @Test
    void sendWarningWebhook_integration() throws NotificationException {
        notifier.notifyAlert(
                AlertSeverity.WARNING,
                "idn-004",
                "[TEST] MatchIdNull warning webhook"
        );
    }
}
