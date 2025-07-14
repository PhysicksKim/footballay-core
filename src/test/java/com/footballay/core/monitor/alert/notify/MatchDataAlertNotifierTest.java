package com.footballay.core.monitor.alert.notify;

import com.footballay.core.monitor.alert.NotificationException;
import com.footballay.core.monitor.alert.manaer.AlertSeverity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles({ "dev", "mocks", "api" })
// @TestPropertySource(properties = "discord.debug-mode=true") // 실제로 알림을 보내고 싶다면 주석을 해제하세요
class MatchDataAlertNotifierTest {

    @Autowired
    private MatchDataAlertNotifier notifier;

    @Test
    void sendSuccessWebhook_integration() throws NotificationException {
        // 성공 알림: 멘션 없이 content만 전송 → Discord 채널에 메시지 확인
        notifier.notifyAlert(
                AlertSeverity.SUCCESS,
                "md-001",
                "[TEST] MatchData success webhook"
        );
    }

    @Test
    void sendFailureWebhook_integration() throws NotificationException {
        // 실패 알림: Developer 롤 멘션 포함 → Discord 채널에 메시지 확인
        notifier.notifyAlert(
                AlertSeverity.FAILURE,
                "md-002",
                "[TEST] MatchData failure webhook"
        );
    }

    @Test
    void sendExceptionWebhook_integration() throws NotificationException {
        // 예외 알림: Developer 롤 멘션 포함 → Discord 채널에 메시지 확인
        notifier.notifyAlert(
                AlertSeverity.EXCEPTION,
                "md-003",
                "[TEST] MatchData exception webhook"
        );
    }
}