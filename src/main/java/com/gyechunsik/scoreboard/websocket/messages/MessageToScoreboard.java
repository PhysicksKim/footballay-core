package com.gyechunsik.scoreboard.websocket.messages;

import lombok.ToString;

import java.util.Map;

/**
 * {
 *   "type": "scoreChange",
 *   "data": {
 *     "team": "A",
 *     "newScore": 3
 *   },
 *   "metadata": {
 *     "timestamp": 1633036800000,
 *     "messageId": "abc123"
 *   }
 * }
 */
@ToString
public class MessageToScoreboard extends BaseWebSocketMessage {
    public MessageToScoreboard(String type, Map<String, Object> data, Map<String, Object> metadata) {
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
