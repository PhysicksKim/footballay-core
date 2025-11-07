package com.footballay.core.domain.football.external.fetch;

import java.util.HashMap;
import java.util.Map;

/**
 * Api Sports 에서는 error가 없는 경우 `[]` 형태로 응답하고,
 * error가 있는 경우 `{ "key": "value", ... }` 형태로 응답합니다.
 * <p>
 * 이를 처리하기 위한 ApiError 클래스입니다.
 */
public record ApiError(Map<String, String> details) {
    public ApiError(Map<String, String> details) {
        this.details = details != null ? details : new HashMap<>();
    }

    public boolean hasError() {
        return !details.isEmpty();
    }

    public String getMessage() {
        return details.getOrDefault("token",
            details.getOrDefault("message",
                details.getOrDefault("error", "Unknown error")));
    }
}
