package com.footballay.core.monitor.alert.notify.discord;

import com.footballay.core.monitor.alert.NotificationException;

public interface DiscordWebhookSender {
    void sendWebhook(String webhookUrl, DiscordWebhookRequest request) throws NotificationException;
}
