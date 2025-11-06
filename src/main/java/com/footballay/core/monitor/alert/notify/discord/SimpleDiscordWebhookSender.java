package com.footballay.core.monitor.alert.notify.discord;

import com.footballay.core.monitor.alert.NotificationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class SimpleDiscordWebhookSender implements DiscordWebhookSender {

    private final RestClient webClient = RestClient.create();

    @Override
    public void sendWebhook(String webhookUrl, DiscordWebhookRequest request) throws NotificationException {
        validateWebhookUrl(webhookUrl);
        try {
            ResponseEntity<String> resp = webClient.post()
                    .uri(webhookUrl)
                    .body(request)
                    .retrieve()
                    .toEntity(String.class);
            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new NotificationException("Failed to send webhook, received status: " + resp.getStatusCode());
            }
        } catch (Exception e) {
            throw new NotificationException("Failed to send webhook to URL: " + webhookUrl, e);
        }
    }

    private void validateWebhookUrl(String webhookUrl) {
        if (!StringUtils.hasText(webhookUrl)) {
            throw new IllegalArgumentException("Webhook URL cannot be null or empty");
        }
    }

}
