package com.footballay.core.websocket.response;

public class HelloResponse {
    private String name;

    public String getName() {
        return this.name;
    }

    public HelloResponse(final String name) {
        this.name = name;
    }

    public HelloResponse() {
    }
}
