package com.gyechunsik.scoreboard.websocket.response;


import lombok.Getter;

import java.util.Map;

@Getter
public class SubscribeDoneResponse {

    private final int code = 200;
    private final String type = "sub";
    private final String message = "subscribeDone";
    private final Map<String, String> data;

    public SubscribeDoneResponse(String destination) {
        this.data = Map.of("destination", destination);
    }
}
