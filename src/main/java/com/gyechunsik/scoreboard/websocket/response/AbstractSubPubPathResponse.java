package com.gyechunsik.scoreboard.websocket.response;

import lombok.Getter;

@Getter
abstract public class AbstractSubPubPathResponse extends AbstractBaseResponse{

    protected final String pubPath;
    protected final String subPath;
    protected final String remoteCode;

    public AbstractSubPubPathResponse(String remoteCode) {
        this(200, "success", remoteCode);
    }

    public AbstractSubPubPathResponse(int code, String message, String remoteCode) {
        super(code, message);
        this.pubPath = "/app/remote/" + remoteCode;
        this.subPath = "/user/topic/remote/" + remoteCode;
        this.remoteCode = remoteCode;
    }
}
