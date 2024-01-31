package com.gyechunsik.scoreboard.websocket.validator;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UniformEnum {
    HOME("home"),
    AWAY("away"),
    THIRD("third");

    private final String value;

    UniformEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
