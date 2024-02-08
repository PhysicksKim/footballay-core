package com.gyechunsik.scoreboard.websocket.service;

import com.gyechunsik.scoreboard.websocket.request.RemoteConnectMessage;
import jakarta.validation.constraints.NotNull;

public class RemoteCodeMapper {

    public static RemoteCode from(@NotNull RemoteConnectMessage remoteConnectMessage) {
        String code = remoteConnectMessage.getRemoteCode();
        return new RemoteCode(code);
    }
}
