package com.footballay.core.monitor.alert.notify;

import com.footballay.core.monitor.alert.NotificationException;
import com.footballay.core.monitor.alert.manaer.AlertCategory;
import com.footballay.core.monitor.alert.manaer.AlertSeverity;
import com.footballay.core.monitor.alert.notify.discord.DiscordMentionTarget;
import com.footballay.core.monitor.alert.notify.discord.DiscordWebhookRequest;
import com.footballay.core.monitor.alert.notify.discord.DiscordWebhookRequestFactory;
import com.footballay.core.monitor.alert.notify.discord.DiscordWebhookSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LineupAlertNotifier implements AlertNotifier {

    private static final Logger log = LoggerFactory.getLogger(LineupAlertNotifier.class);

    private final DiscordWebhookSender discordWebhookSender;

    private final DiscordWebhookRequestFactory requestFactory;

    /**
     * 디버그 모드가 활성화된 경우 dev profile 환경이더라도 무조건 알림을 보냅니다.
     * 프로덕션 환경에서는 항상 알림을 보냅니다.
     */
    @Value("${discord.debug-mode:false}")
    private boolean DEBUG_MODE;

    @Value("#{environment.acceptsProfiles('dev')}")
    private boolean devProfileActive;

    @Value("${discord.webhook.lineup:}")
    private String DISCORD_LINEUP_WEBHOOK_URL;

    public LineupAlertNotifier(DiscordWebhookSender discordWebhookSender,
                               DiscordWebhookRequestFactory requestFactory) {
        this.discordWebhookSender = discordWebhookSender;
        this.requestFactory = requestFactory;
    }

    @Override
    public void notifyAlert(AlertSeverity severity, String fixtureId, String message) throws NotificationException {
        if (devProfileActive && !DEBUG_MODE) {
            log.info("Skipping success notification in non-production profile");
            return;
        }

        if(severity.equals(AlertSeverity.SUCCESS)) {
            notifySuccess(fixtureId, message);
        } else if(severity.equals(AlertSeverity.FAILURE)) {
            notifyFailure(fixtureId, message);
        } else if(severity.equals(AlertSeverity.EXCEPTION)) {
            notifyException(fixtureId, message);
        } else {
            throw new NotificationException("Unsupported alert severity: " + severity);
        }
    }

    @Override
    public boolean isSupport(AlertCategory category) {
        return category.equals(AlertCategory.LINEUP);
    }

    private void notifySuccess(String fixtureId, String message) throws NotificationException {
        String msg = "Fixture " + fixtureId + " - " + message;
        DiscordWebhookRequest request = requestFactory.createRequest(msg, List.of());
        discordWebhookSender.sendWebhook(DISCORD_LINEUP_WEBHOOK_URL, request);
    }

    private void notifyFailure(String fixtureId, String message) throws NotificationException {
        String msg = "Fixture " + fixtureId + " - " + message;
        DiscordWebhookRequest request = requestFactory.createRequest(msg, List.of(DiscordMentionTarget.DEVELOPER));
        discordWebhookSender.sendWebhook(DISCORD_LINEUP_WEBHOOK_URL, request);
    }

    private void notifyException(String fixtureId, String errorMessage) throws NotificationException {
        String msg = "Fixture " + fixtureId + " encountered an error: " + errorMessage;
        DiscordWebhookRequest request = requestFactory.createRequest(msg, List.of(DiscordMentionTarget.DEVELOPER));
        discordWebhookSender.sendWebhook(DISCORD_LINEUP_WEBHOOK_URL, request);
    }

}
