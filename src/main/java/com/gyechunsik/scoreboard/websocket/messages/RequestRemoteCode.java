package com.gyechunsik.scoreboard.websocket.messages;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RequestRemoteCode {

    private String recipientId;

    public RequestRemoteCode(String message) {
        this.recipientId = message;
    }
}
