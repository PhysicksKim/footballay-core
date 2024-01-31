package com.gyechunsik.scoreboard.websocket.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Map;

@Getter(AccessLevel.PUBLIC)
public abstract class BaseWebSocketMessage {
    @NotNull
    @JsonProperty("type")
    protected String type; // score, uniform, injury, check(요청이 제대로 수행됐는지 체크), state(scoreboard 의 상태 확인), connection(연결 확인), 등
    @JsonProperty("data")
    protected Map<String, Object> data;
    @JsonProperty("metadata")
    protected Map<String, Object> metadata;
}

/*
    type : score
    data : {
        teamA : 1,
        teamB : 2
    }

    type : uniform
    data : {
        teamA : "home",
        teamB : "third"
    }

    type : injury
    data : {
        given : 7
    }

    ------
    metadata : {
        timestamp : 1633036800000,
        messageId : "abc123"
    }
 */