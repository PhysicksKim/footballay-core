package com.gyechunsik.scoreboard.domain.defaultmatch.entity.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum LeagueCategory {
    epl2324("EPL 2023-24"),
    nation("Nations"),
    etc("ETC");

    private final String value;

    LeagueCategory(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static LeagueCategory of(String value) {
        return Arrays.stream(LeagueCategory.values())
                .filter(category -> category.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid LeagueCategory value: " + value));
    }
}
