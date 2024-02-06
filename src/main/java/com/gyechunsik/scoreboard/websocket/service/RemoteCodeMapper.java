package com.gyechunsik.scoreboard.websocket.service;

import com.gyechunsik.scoreboard.websocket.request.RemoteConnectMessage;
import jakarta.validation.constraints.NotNull;

public class RemoteCodeMapper {

    public static RemoteCode from(@NotNull RemoteConnectMessage remoteConnectMessage) {
        String code = remoteConnectMessage.getCodeValue();
        return new RemoteCode(code);
    }
}
