package com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.service;

import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.RemoteCode;
import com.gyechunsik.scoreboard.websocket.request.RemoteConnectRequestMessage;
import jakarta.validation.constraints.NotNull;

public class RemoteCodeMapper {

    public static RemoteCode from(@NotNull RemoteConnectRequestMessage remoteConnectRequestMessage) {
        String code = remoteConnectRequestMessage.getRemoteCode();
        return RemoteCode.of(code);
    }
}
