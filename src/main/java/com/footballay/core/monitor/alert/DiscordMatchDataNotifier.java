package com.footballay.core.monitor.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Discord를 통해 매치 데이터 알림을 전송하는 서비스 구현체입니다.
 * 프로덕션 환경에서만 알림을 전송하며, 디버그 모드가 활성화된 경우 개발 환경에서도 알림을 전송합니다.
 */
@Service
public class DiscordMatchDataNotifier implements MatchDataNotifier {

    private static final Logger log = LoggerFactory.getLogger(DiscordMatchDataNotifier.class);

    /**
     * 디버그 모드가 활성화된 경우 dev profile 환경이더라도 무조건 알림을 보냅니다.
     * 프로덕션 환경에서는 항상 알림을 보냅니다.
     */
    @Value("${discord.debug-mode:false}")
    private boolean DEBUG_MODE;

    @Value("#{environment.acceptsProfiles('dev')}")
    private boolean devProfileActive;

    @Value("${discord.webhook.success:}")
    private String DISCORD_LINEUP_SUCCESS;

    @Value("${discord.webhook.failure:}")
    private String DISCORD_LINEUP_FAILURE;

    @Value("${discord.webhook.exception:}")
    private String DISCORD_FIXTURE_EXCEPTION;

    private final RestClient webClient = RestClient.create();

    @Override
    public void notifySuccess(String fixtureId, String message) throws NotificationException {
        if (devProfileActive && !DEBUG_MODE) {
            log.info("Skipping success notification in non-production profile");
            return;
        }
        String content = "Fixture " + fixtureId + " - " + message;
        sendWebhook(DISCORD_LINEUP_SUCCESS, content);
    }

    @Override
    public void notifyFailure(String fixtureId, String message) throws NotificationException {
        if (devProfileActive && !DEBUG_MODE) {
            log.info("Skipping failure notification in non-production profile");
            return;
        }
        String content = "Fixture " + fixtureId + " - " + message;
        sendWebhook(DISCORD_LINEUP_FAILURE, content);
    }

    @Override
    public void notifyException(String fixtureId, String errorMessage) throws NotificationException {
        if (devProfileActive && !DEBUG_MODE) {
            log.info("Skipping exception notification in non-production profile");
            return;
        }
        String content = "Fixture " + fixtureId + " encountered an error: " + errorMessage;
        sendWebhook(DISCORD_FIXTURE_EXCEPTION, content);
    }

    private void sendWebhook(String url, String content) throws NotificationException {
        validateWebhookUrl(url);
        try {
            webClient.post()
                    .uri(url)
                    .body(Map.of("content", content))
                    .retrieve()
                    .toEntity(String.class);
        } catch (Exception ex) {
            throw new NotificationException("Failed to send webhook to URL: " + url, ex);
        }
    }

    private void validateWebhookUrl(String webhookUrl) {
        if (!StringUtils.hasText(webhookUrl)) {
            throw new IllegalArgumentException("Webhook URL cannot be null or empty");
        }
    }
}
