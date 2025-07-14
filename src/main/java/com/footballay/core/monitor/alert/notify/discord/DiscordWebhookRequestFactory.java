package com.footballay.core.monitor.alert.notify.discord;

import java.util.List;

public interface DiscordWebhookRequestFactory {

    DiscordWebhookRequest createRequest(String content,
                                        List<DiscordMentionTarget> mentionTargets);

}
