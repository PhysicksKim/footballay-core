package com.gyechunsik.scoreboard.web.admin.football.response;

public record LiveStatusResponse(
        String longStatus,
        String shortStatus,
        Integer elapsed) {
}

