package com.footballay.core.monitor.alert.notify.discord;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DiscordWebhookRequest {
    private final String content;

    /**
     * JSON 으로는 "allowed_mentions"로 직렬화됩니다.
     */
    @JsonProperty("allowed_mentions")
    private final AllowedMentions allowedMentions;

    private DiscordWebhookRequest(Builder builder) {
        this.content = builder.content;
        this.allowedMentions = builder.allowedMentions;
    }

    public String getContent() {
        return content;
    }

    public AllowedMentions getAllowedMentions() {
        return allowedMentions;
    }

    /**
     * "allowed_mentions" 오브젝트를 캡슐화합니다.
     */
    public static class AllowedMentions {
        private final List<String> roles;

        public AllowedMentions(List<String> roles) {
            this.roles = roles;
        }

        public List<String> getRoles() {
            return roles;
        }

        /**
         * 주어진 롤 ID 목록만 멘션을 허용하고,
         * parse에 "roles" 옵션을 설정합니다.
         */
        public static AllowedMentions forRoles(List<String> roles) {
            return new AllowedMentions(
                    roles == null ? Collections.emptyList() : roles
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String content;
        private AllowedMentions allowedMentions = new AllowedMentions(Collections.emptyList());

        /** 메시지 본문 설정 */
        public Builder content(String content) {
            this.content = content;
            return this;
        }

        /**
         * 허용할 멘션(roles) 및 parse 옵션 설정.
         * 보통 AllowedMentions.forRoles(...)를 사용하세요.
         */
        public Builder allowedMentions(AllowedMentions allowedMentions) {
            this.allowedMentions = Objects.requireNonNull(allowedMentions);
            return this;
        }

        public DiscordWebhookRequest build() {
            if (content == null) {
                throw new IllegalStateException("content must not be null");
            }
            return new DiscordWebhookRequest(this);
        }
    }
}