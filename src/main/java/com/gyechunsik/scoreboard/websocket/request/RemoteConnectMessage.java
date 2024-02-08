package com.gyechunsik.scoreboard.websocket.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RemoteConnectMessage {

    private String remoteCode;

    public RemoteConnectMessage(@JsonProperty("remoteCode") String remoteCode) {
        this.remoteCode = remoteCode;
    }
}
