package com.footballay.core.websocket.response;

public abstract class AbstractBaseResponse {
    protected final int code;
    protected final String message;

    public AbstractBaseResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
