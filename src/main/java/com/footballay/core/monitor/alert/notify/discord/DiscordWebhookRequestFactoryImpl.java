package com.footballay.core.monitor.alert.notify.discord;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class DiscordWebhookRequestFactoryImpl implements DiscordWebhookRequestFactory {

    @Value("${discord.role.developer:}")
    private String developerRoleId;

    @Override
    public DiscordWebhookRequest createRequest(String content,
                                               List<DiscordMentionTarget> mentionTargets) {
        StringBuilder prefix = new StringBuilder();
        List<String> roleIds = new ArrayList<>();

        for (DiscordMentionTarget target : mentionTargets) {
            String roleId = switch (target) {
                case DEVELOPER -> developerRoleId;
            };
            if (StringUtils.hasText(roleId)) {
                prefix.append("<@&").append(roleId).append("> ");
                roleIds.add(roleId);
            }
        }

        DiscordWebhookRequest.Builder builder = DiscordWebhookRequest.builder()
                .content(prefix + content);

        if (!roleIds.isEmpty()) {
            builder.allowedMentions(
                    DiscordWebhookRequest.AllowedMentions.forRoles(roleIds)
            );
        }

        return builder.build();
    }
}