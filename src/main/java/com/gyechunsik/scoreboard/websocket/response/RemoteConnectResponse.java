package com.gyechunsik.scoreboard.websocket.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Remote client 가 받는 응답 메세지입니다. pubPath 를 포함합니다.
 * pubPath 를 통해서 message 를 보내면 Board client 에게 명령을 보낼 수 있습니다.
 */
@Getter
@NoArgsConstructor
public class RemoteConnectResponse extends SuccessResponse {

    protected String pubPath;

    public RemoteConnectResponse(int code, String message, String remoteCode) {
        super(code, message);
        this.pubPath = "/app/board." + remoteCode;
    }
}
