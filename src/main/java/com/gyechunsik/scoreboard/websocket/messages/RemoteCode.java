package com.gyechunsik.scoreboard.websocket.messages;

import lombok.Getter;

@Getter
public class RemoteCode {

    private String code;

    public RemoteCode(String code) {
        this.code = code;
    }
}
