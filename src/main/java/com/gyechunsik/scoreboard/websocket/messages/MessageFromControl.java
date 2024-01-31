package com.gyechunsik.scoreboard.websocket.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * validation 은 3가지 과정을 통해 이뤄진다
 * 1. type 을 검증한다
 * 2. type 별 data 구조를 가져와서 key 검증한다
 * 3. metadata 구조는 고정적임.
 */
public class MessageFromControl extends BaseWebSocketMessage {
    @JsonCreator
    public MessageFromControl(@JsonProperty("type") String type,
                              @JsonProperty("data") Map<String, Object> data,
                              @JsonProperty("metadata") Map<String, Object> metadata) {
        this.type = type;
        this.data = data;
        this.metadata = metadata;
    }
}

/*
    type : score
    data : {
        teamA : 1,
        teamB : 2
    }
    metadata : {
        timestamp : 1633036800000,
        messageId : "abc123"
    }

    type : uniform
    data : {
        teamA : "home",
        teamB : "third"
    }
    metadata : {
        timestamp : 1633036800000,
        messageId : "abc123"
    }

    type : injury
    data : {
        given : 7
    }
    metadata : {
        timestamp : 1633036800000,
        messageId : "abc123"
    }

 */
