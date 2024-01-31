package com.gyechunsik.scoreboard.websocket.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class MessageFromScoreboard extends BaseWebSocketMessage {
    @JsonCreator
    public MessageFromScoreboard(@JsonProperty("type") String type,
                              @JsonProperty("data") Map<String, Object> data,
                              @JsonProperty("metadata") Map<String, Object> metadata) {
        this.type = type;
        this.data = data;
        this.metadata = metadata;
    }
}