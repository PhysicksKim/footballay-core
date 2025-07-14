package com.footballay.core.monitor.alert.notify;

import com.footballay.core.monitor.alert.NotificationException;
import com.footballay.core.monitor.alert.manaer.AlertCategory;
import com.footballay.core.monitor.alert.manaer.AlertSeverity;
import com.footballay.core.monitor.alert.notify.discord.DiscordMentionTarget;
import com.footballay.core.monitor.alert.notify.discord.DiscordWebhookRequest;
import com.footballay.core.monitor.alert.notify.discord.DiscordWebhookRequestFactory;
import com.footballay.core.monitor.alert.notify.discord.DiscordWebhookSender;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchIdNullAlertNotifier implements AlertNotifier {

    private static final Logger log = LoggerFactory.getLogger(MatchIdNullAlertNotifier.class);

    /**
     * 디버그 모드가 활성화된 경우 dev profile 환경이더라도 무조건 알림을 보냅니다.
     * 프로덕션 환경에서는 항상 알림을 보냅니다.
     */
    @Value("${discord.debug-mode:false}")
    private boolean DEBUG_MODE;

    @Value("#{environment.acceptsProfiles('dev')}")
    private boolean devProfileActive;

    private final DiscordWebhookSender discordWebhookSender;

    private final DiscordWebhookRequestFactory requestFactory;

    @Value("${discord.webhook.id-null:}")
    private String DISCORD_MATCHID_NULL;

    public MatchIdNullAlertNotifier(DiscordWebhookSender discordWebhookSender,
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

        if (severity.equals(AlertSeverity.SUCCESS)) {
            notifySuccess(fixtureId, message);
        } else if (severity.equals(AlertSeverity.FAILURE)) {
            notifyFailure(fixtureId, message);
        } else if (severity.equals(AlertSeverity.EXCEPTION)) {
            notifyException(fixtureId, message);
        } else if (severity.equals(AlertSeverity.WARNING)) {
            notifyWarning(fixtureId, message);
        } else {
            throw new NotificationException("Unsupported alert severity: " + severity);
        }
    }

    @Override
    public boolean isSupport(AlertCategory category) {
        return category.equals(AlertCategory.MATCHIDNULL);
    }

    private void notifySuccess(String fixtureId, String message) {
        String msg = "Success: " + fixtureId + "\n" + message;
        DiscordWebhookRequest request = requestFactory.createRequest(msg, List.of());
        log.info("Sending success notification for fixture {}: {}", fixtureId, message);
        discordWebhookSender.sendWebhook(DISCORD_MATCHID_NULL, request);
    }

    private void notifyFailure(String fixtureId, String message) {
        String msg = "Failure: " + fixtureId + "\n" + message;
        DiscordWebhookRequest request = requestFactory.createRequest(msg, List.of(DiscordMentionTarget.DEVELOPER));
        log.info("Sending failure notification for fixture {}: {}", fixtureId, message);
        discordWebhookSender.sendWebhook(DISCORD_MATCHID_NULL, request);
    }

    private void notifyException(String fixtureId, String message) {
        String msg = "Exception: " + fixtureId + "\n" + message;
        DiscordWebhookRequest request = requestFactory.createRequest(msg, List.of(DiscordMentionTarget.DEVELOPER));
        log.info("Sending exception notification for fixture {}: {}", fixtureId, message);
        discordWebhookSender.sendWebhook(DISCORD_MATCHID_NULL, request);
    }

    private void notifyWarning(String fixtureId, String message) {
        String msg = "Warning: " + fixtureId + "\n" + message;
        DiscordWebhookRequest request = requestFactory.createRequest(msg, List.of());
        log.info("Sending warning notification for fixture {}: {}", fixtureId, message);
        discordWebhookSender.sendWebhook(DISCORD_MATCHID_NULL, request);
    }

}
