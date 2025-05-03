package com.footballay.core.web.admin.football.response;

public record LiveStatusResponse(
        String longStatus,
        String shortStatus,
        Integer elapsed) {
}

