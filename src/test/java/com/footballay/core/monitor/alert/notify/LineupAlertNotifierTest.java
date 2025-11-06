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
@ActiveProfiles({"dev", "api"})
@TestPropertySource(properties = "discord.debug-mode=true")
@Disabled("실제 Discord 웹훅을 사용하므로, 필요 시에만 활성화하세요.")
class LineupAlertNotifierTest {

    @Autowired
    LineupAlertNotifier lineupAlertNotifier;

    @Test
    void sendFailureWebhook_integration() throws NotificationException {
        // 실패 알림: Developer 롤 멘션 포함 → 실제 Discord 채널에 메시지 확인
        lineupAlertNotifier.notifyAlert(
                AlertSeverity.FAILURE,
                "test-001",
                "[TEST] for failure webhook"
        );
    }

    @Test
    void sendSuccessWebhook_integration() throws NotificationException {
        // 성공 알림: 멘션 없이 content만 전송 → 채널 메시지만 확인
        lineupAlertNotifier.notifyAlert(
                AlertSeverity.SUCCESS,
                "test-002",
                "[TEST] for success webhook"
        );
    }

    @Test
    void sendExceptionWebhook_integration() throws NotificationException {
        // 예외 알림: Developer 롤 멘션 포함 → 실제 Discord 채널에 메시지 확인
        lineupAlertNotifier.notifyAlert(
                AlertSeverity.EXCEPTION,
                "test-003",
                "[TEST] exception webhook"
        );
    }
}
