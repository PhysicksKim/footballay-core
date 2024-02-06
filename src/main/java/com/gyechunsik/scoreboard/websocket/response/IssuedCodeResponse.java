package com.gyechunsik.scoreboard.websocket.response;

import lombok.Getter;

@Getter
public class IssuedCodeResponse extends SuccessResponse {

    protected final String remoteCode;
    protected final String subPath;

    public IssuedCodeResponse(String code, String message, String remoteCode) {
        super(code, message);
        this.remoteCode = remoteCode;
        this.subPath = "/topic/board.{"+remoteCode+"}";
    }
}
