package com.gyechunsik.scoreboard.websocket.response;

import lombok.Getter;

@Getter
abstract public class AbstractSubPubPathResponse extends AbstractBaseResponse{

    protected final String pubPath;
    protected final String subPath;

    public AbstractSubPubPathResponse(String remoteCode) {
        super(200, "success");
        this.pubPath = "/topic/remote/" + remoteCode;
        this.subPath = "/app/remote/" + remoteCode;
    }

    public AbstractSubPubPathResponse(int code, String message, String remoteCode) {
        super(code, message);
        this.pubPath = "/topic/remote/" + remoteCode;
        this.subPath = "/app/remote/" + remoteCode;
    }
}
