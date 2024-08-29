package com.gyechunsik.scoreboard.web.admin.football.request;

public record PreventUnlinkRequest(
        Long playerId,
        Boolean preventUnlink
) {
}
