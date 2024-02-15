package com.gyechunsik.scoreboard.websocket.service;

import com.gyechunsik.scoreboard.websocket.request.RemoteConnectRequestMessage;
import jakarta.validation.constraints.NotNull;

public class RemoteCodeMapper {

    public static RemoteCode from(@NotNull RemoteConnectRequestMessage remoteConnectRequestMessage) {
        String code = remoteConnectRequestMessage.getRemoteCode();
        return new RemoteCode(code);
    }
}
