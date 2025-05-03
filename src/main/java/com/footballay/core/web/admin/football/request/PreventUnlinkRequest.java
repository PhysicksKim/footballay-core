package com.footballay.core.web.admin.football.request;

public record PreventUnlinkRequest(
        Long playerId,
        Boolean preventUnlink
) {
}
