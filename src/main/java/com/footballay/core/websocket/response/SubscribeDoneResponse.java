package com.footballay.core.websocket.response;

import java.util.Map;

public class SubscribeDoneResponse {
    private final int code = 200;
    private final String type = "sub";
    private final String message = "subscribeDone";
    private final Map<String, String> data;

    public SubscribeDoneResponse(String destination) {
        this.data = Map.of("destination", destination);
    }

    public int getCode() {
        return this.code;
    }

    public String getType() {
        return this.type;
    }

    public String getMessage() {
        return this.message;
    }

    public Map<String, String> getData() {
        return this.data;
    }
}
