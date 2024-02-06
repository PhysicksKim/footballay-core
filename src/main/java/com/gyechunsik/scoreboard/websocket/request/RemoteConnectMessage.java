package com.gyechunsik.scoreboard.websocket.request;

import lombok.Getter;

@Getter
public class RemoteConnectMessage {

    private final String remoteCode;

    public RemoteConnectMessage(String remoteCode) {
        this.remoteCode = remoteCode;
    }
}
