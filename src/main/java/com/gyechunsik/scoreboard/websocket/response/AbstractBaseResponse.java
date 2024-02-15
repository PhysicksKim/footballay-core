package com.gyechunsik.scoreboard.websocket.response;

import lombok.Getter;

@Getter
abstract public class AbstractBaseResponse {

    protected final int code;
    protected final String message;

    public AbstractBaseResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
